package cdv.bifrost.workspace.service;

import cdv.bifrost.auth.model.entity.UserEntity;
import cdv.bifrost.auth.repository.UserRepository;
import cdv.bifrost.authorization.model.entity.RoleEntity;
import cdv.bifrost.authorization.repository.RoleRepository;
import cdv.bifrost.workspace.model.entity.WorkspaceEntity;
import cdv.bifrost.workspace.model.entity.WorkspaceInvitationEntity;
import cdv.bifrost.workspace.model.entity.WorkspaceMemberEntity;
import cdv.bifrost.workspace.model.entity.WorkspaceMemberId;
import cdv.bifrost.workspace.model.enums.WorkspaceInvitationStatus;
import cdv.bifrost.workspace.model.request.CreateWorkspaceInvitationRequest;
import cdv.bifrost.workspace.model.response.WorkspaceInvitationResponse;
import cdv.bifrost.workspace.model.response.WorkspaceMemberResponse;
import cdv.bifrost.workspace.repository.WorkspaceInvitationRepository;
import cdv.bifrost.workspace.repository.WorkspaceMemberRepository;
import cdv.bifrost.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WorkspaceInvitationService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES = 32;
    private static final int INVITATION_EXPIRY_DAYS = 7;

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final WorkspaceInvitationRepository workspaceInvitationRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final Clock clock = Clock.systemUTC();

    @Value("${bifrost.web.invitation-base-url:http://localhost:3000/workspace-invitations/accept}")
    private String invitationBaseUrl;

    @Transactional
    public WorkspaceInvitationResponse createInvitation(
            UUID workspaceId,
            CreateWorkspaceInvitationRequest request,
            UserEntity currentUser
    ) {
        WorkspaceEntity workspace = getWorkspace(workspaceId);
        requireWorkspaceManager(workspaceId, currentUser);

        String normalizedEmail = normalizeEmail(request.email());
        RoleEntity invitedRole = getInvitableRole(request.roleName());

        userRepository.findByEmailIgnoreCase(normalizedEmail)
                .ifPresent(user -> {
                    if (workspaceMemberRepository.existsByWorkspace_IdAndUser_Id(workspaceId, user.getId())) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already a workspace member");
                    }
                });

        workspaceInvitationRepository.findByWorkspace_IdAndEmailIgnoreCaseAndStatus(
                workspaceId,
                normalizedEmail,
                WorkspaceInvitationStatus.PENDING
        ).ifPresent(invitation -> {
            invitation.setStatus(WorkspaceInvitationStatus.REVOKED);
            invitation.setTokenHash(hashToken(generateToken()));
            workspaceInvitationRepository.saveAndFlush(invitation);
        });

        String token = generateToken();
        WorkspaceInvitationEntity invitation = WorkspaceInvitationEntity.builder()
                .workspace(workspace)
                .email(normalizedEmail)
                .role(invitedRole)
                .invitedByUser(currentUser)
                .tokenHash(hashToken(token))
                .status(WorkspaceInvitationStatus.PENDING)
                .expiresAt(now().plusDays(INVITATION_EXPIRY_DAYS))
                .build();

        WorkspaceInvitationEntity savedInvitation = workspaceInvitationRepository.save(invitation);
        return WorkspaceInvitationResponse.from(savedInvitation, buildInviteUrl(token));
    }

    @Transactional(readOnly = true)
    public List<WorkspaceInvitationResponse> listInvitations(UUID workspaceId, UserEntity currentUser) {
        requireWorkspaceManager(workspaceId, currentUser);

        return workspaceInvitationRepository.findByWorkspace_IdOrderByCreatedAtDesc(workspaceId)
                .stream()
                .map(invitation -> WorkspaceInvitationResponse.from(invitation, null))
                .toList();
    }

    @Transactional
    public void revokeInvitation(UUID workspaceId, UUID invitationId, UserEntity currentUser) {
        requireWorkspaceManager(workspaceId, currentUser);

        WorkspaceInvitationEntity invitation = workspaceInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation not found"));
        if (!invitation.getWorkspace().getId().equals(workspaceId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation not found");
        }
        if (invitation.getStatus() != WorkspaceInvitationStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only pending invitations can be revoked");
        }

        invitation.setStatus(WorkspaceInvitationStatus.REVOKED);
    }

    @Transactional
    public WorkspaceMemberResponse acceptInvitation(String token, UserEntity currentUser) {
        WorkspaceInvitationEntity invitation = workspaceInvitationRepository.findByTokenHash(hashToken(token))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation not found"));

        if (invitation.getStatus() != WorkspaceInvitationStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Invitation is not pending");
        }
        if (invitation.getExpiresAt().isBefore(now())) {
            invitation.setStatus(WorkspaceInvitationStatus.EXPIRED);
            throw new ResponseStatusException(HttpStatus.GONE, "Invitation has expired");
        }
        if (!normalizeEmail(currentUser.getEmail()).equals(invitation.getEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invitation email does not match authenticated user");
        }

        UUID workspaceId = invitation.getWorkspace().getId();
        if (workspaceMemberRepository.existsByWorkspace_IdAndUser_Id(workspaceId, currentUser.getId())) {
            invitation.setStatus(WorkspaceInvitationStatus.ACCEPTED);
            invitation.setAcceptedAt(now());
            WorkspaceMemberEntity existingMember = workspaceMemberRepository
                    .findByWorkspace_IdAndUser_Id(workspaceId, currentUser.getId())
                    .orElseThrow();
            return WorkspaceMemberResponse.from(existingMember);
        }

        WorkspaceMemberEntity member = WorkspaceMemberEntity.builder()
                .id(new WorkspaceMemberId(workspaceId, currentUser.getId()))
                .workspace(invitation.getWorkspace())
                .user(currentUser)
                .role(invitation.getRole())
                .build();

        WorkspaceMemberEntity savedMember = workspaceMemberRepository.save(member);
        invitation.setStatus(WorkspaceInvitationStatus.ACCEPTED);
        invitation.setAcceptedAt(now());
        return WorkspaceMemberResponse.from(savedMember);
    }

    private WorkspaceEntity getWorkspace(UUID workspaceId) {
        return workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workspace not found"));
    }

    private void requireWorkspaceManager(UUID workspaceId, UserEntity currentUser) {
        WorkspaceMemberEntity member = workspaceMemberRepository.findByWorkspace_IdAndUser_Id(workspaceId, currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Workspace membership is required"));

        String roleName = member.getRole().getName();
        if (!"OWNER".equals(roleName) && !"ADMIN".equals(roleName)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Workspace manager role is required");
        }
    }

    private RoleEntity getInvitableRole(String roleName) {
        String normalizedRoleName = roleName.trim().toUpperCase(Locale.ROOT);
        if (!"ADMIN".equals(normalizedRoleName) && !"MEMBER".equals(normalizedRoleName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invitable role must be ADMIN or MEMBER");
        }

        return roleRepository.findByName(normalizedRoleName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role does not exist"));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String generateToken() {
        byte[] tokenBytes = new byte[TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private String buildInviteUrl(String token) {
        String separator = invitationBaseUrl.contains("?") ? "&" : "?";
        return invitationBaseUrl + separator + "token=" + token;
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now(clock);
    }
}
