package cdv.bifrost.workspace.service;

import cdv.bifrost.auth.exception.RoleNotFound;
import cdv.bifrost.auth.model.Role;
import cdv.bifrost.auth.model.User;
import cdv.bifrost.auth.model.entity.UserEntity;
import cdv.bifrost.auth.model.mapper.RoleEntityToRoleMapper;
import cdv.bifrost.auth.model.mapper.UserEntityToDomainMapper;
import cdv.bifrost.auth.repository.UserRepository;
import cdv.bifrost.authorization.model.entity.RoleEntity;
import cdv.bifrost.authorization.repository.RoleRepository;
import cdv.bifrost.common.security.Identity;
import cdv.bifrost.workspace.model.Workspace;
import cdv.bifrost.workspace.model.WorkspaceInvitation;
import cdv.bifrost.workspace.model.WorkspaceMember;
import cdv.bifrost.workspace.model.entity.WorkspaceEntity;
import cdv.bifrost.workspace.model.entity.WorkspaceInvitationEntity;
import cdv.bifrost.workspace.model.entity.WorkspaceMemberEntity;
import cdv.bifrost.workspace.model.entity.WorkspaceMemberId;
import cdv.bifrost.workspace.model.enums.WorkspaceInvitationStatus;
import cdv.bifrost.workspace.model.mapper.*;
import cdv.bifrost.workspace.model.request.WorkspaceCreateRequest;
import cdv.bifrost.workspace.model.request.WorkspaceMemberAddRequest;
import cdv.bifrost.workspace.model.request.WorkspaceMemberUpdateRequest;
import cdv.bifrost.workspace.model.request.WorkspaceUpdateRequest;
import cdv.bifrost.workspace.repository.WorkspaceInvitationRepository;
import cdv.bifrost.workspace.repository.WorkspaceMemberRepository;
import cdv.bifrost.workspace.repository.WorkspaceRepository;
import com.github.slugify.Slugify;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkspaceManagementService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final WorkspaceInvitationRepository workspaceInvitationRepository;
    private final WorkspaceEntityToWorkspaceMapper workspaceEntityToWorkspaceMapper;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    final Slugify slg = Slugify.builder().build();
    private final RoleEntityToRoleMapper roleEntityToRoleMapper;
    private final WorkspaceToWorkspaceEntityMapper workspaceToWorkspaceEntityMapper;
    private final WorkspaceInvitationToWorkspaceInvitationEntityMapper workspaceInvitationToWorkspaceInvitationEntityMapper;
    private final WorkspaceMemberEntityToWorkspaceMemberMapper workspaceMemberEntityToWorkspaceMemberMapper;
    private final UserEntityToDomainMapper userEntityToDomainMapper;
    private final WorkspaceMemberToWorkspaceMemberEntityMapper workspaceMemberToWorkspaceMemberEntityMapper;

    @Transactional(readOnly = true)
    public List<Workspace> getAllWorkspaces(Identity identity) {
        UUID userId = UUID.fromString(identity.getId());

        return workspaceMemberRepository.findByUser_Id(userId).stream()
                .map(WorkspaceMemberEntity::getWorkspace)
                .map(workspaceEntityToWorkspaceMapper::map)
                .toList();
    }

    @Transactional
    public Workspace createWorkspace(WorkspaceCreateRequest request, Identity identity) {
        Workspace workspace = Workspace.builder()
                .name(request.getName())
                .slug(slg.slugify(request.getName()))
                .build();

        WorkspaceEntity workspaceEntity = workspaceRepository.save(workspaceToWorkspaceEntityMapper.map(workspace));
        WorkspaceEntity saved = workspaceRepository.save(workspaceEntity);

        // also could be used as -> var entry : request.getMembers().entrySet()
        for (Map.Entry<String, String> entry : request.getMembers().entrySet()) {
            Role role = roleRepository.findByName(entry.getValue())
                    .map(roleEntityToRoleMapper::map)
                    .orElseThrow(() -> new RoleNotFound(entry.getValue()));

            Optional<UserEntity> userEntity = userRepository.findByEmailIgnoreCase(entry.getKey());
            if (userEntity.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found");
            }
            boolean isMember = workspaceMemberRepository.existsByWorkspace_IdAndUser_Id(saved.getId(), userEntity.get().getId());

            if (isMember) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is already a member of this workspace");
            }

            workspaceInvitationRepository
                    .findByWorkspace_IdAndEmailIgnoreCaseAndStatus(
                            saved.getId(),
                            entry.getKey(),
                            WorkspaceInvitationStatus.PENDING)
                    .ifPresent(invitation -> {
                        invitation.setStatus(WorkspaceInvitationStatus.REVOKED);
                        workspaceInvitationRepository.saveAndFlush(invitation);
                    });

            WorkspaceInvitation invitation = WorkspaceInvitation.builder()
                    .tokenHash(UUID.randomUUID().toString())
                    .email(userEntity.get().getEmail())
                    .role(role)
                    .build();

            WorkspaceInvitationEntity workspaceInvitationEntity = workspaceInvitationToWorkspaceInvitationEntityMapper.map(invitation);
            workspaceInvitationRepository.save(workspaceInvitationEntity);
        }
        return workspaceEntityToWorkspaceMapper.map(saved);
    }

    @Transactional
    public Workspace updateWorkspace(UUID workspaceId, WorkspaceUpdateRequest request) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .map(workspaceEntityToWorkspaceMapper::map)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));
        workspace.setName(request.getName());

        workspaceRepository.save(workspaceToWorkspaceEntityMapper.map(workspace));
        return workspace;
    }

    //TODO: also should be removed related entities such as members and servers
    @Transactional
    public void deleteWorkspace(UUID workspaceId) {
        workspaceRepository.deleteById(workspaceId);
    }

    @Transactional(readOnly = true)
    public List<WorkspaceMember> getAllWorkspaceMembers(Identity identity, UUID workspaceId) {

        boolean existWorkspace = workspaceMemberRepository.existsByWorkspace_IdAndUser_Id(workspaceId, UUID.fromString(identity.getId()));
        if (!existWorkspace) {
            throw new AccessDeniedException("User is not a member of this workspace");
        }
        return workspaceMemberRepository
                .findByWorkspace_Id(workspaceId)
                .stream()
                .map(workspaceMemberEntityToWorkspaceMemberMapper::map).toList();

    }

    @Transactional
    public WorkspaceMember addWorkspaceMember(UUID workspaceId, WorkspaceMemberAddRequest request) {

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .map(workspaceEntityToWorkspaceMapper::map)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workspace not found"));

        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .map(userEntityToDomainMapper::map)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found"));

        if (workspaceMemberRepository.existsByWorkspace_IdAndUser_Id(
                workspaceId, UUID.fromString(user.getId()))) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "User is already a workspace member");
        }

        Role role = roleRepository.findByName(request.getRole())
                .map(roleEntityToRoleMapper::map)
                .orElseThrow(() -> new RoleNotFound(request.getRole()));

        WorkspaceMemberId workspaceMemberId = WorkspaceMemberId.builder()
                .userId(UUID.fromString(user.getId()))
                .workspaceId(UUID.fromString(workspace.getId()))
                .build();

        WorkspaceMember member = WorkspaceMember.builder()
                .workspace(workspace)
                .user(user)
                .role(role)
                .build();

        WorkspaceMemberEntity workspaceMemberEntity = workspaceMemberToWorkspaceMemberEntityMapper.map(member);
        WorkspaceMemberEntity savedMember = workspaceMemberRepository.save(workspaceMemberEntity);
        return workspaceMemberEntityToWorkspaceMemberMapper.map(savedMember);
    }

    @Transactional
    public void updateWorkspaceMember(UUID workspaceId, UUID memberId, WorkspaceMemberUpdateRequest request) {
        WorkspaceMemberEntity member = workspaceMemberRepository.findById(new WorkspaceMemberId(workspaceId, memberId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));

        RoleEntity role = roleRepository.findByName(request.getRole()).orElseThrow(() -> new RoleNotFound(request.getRole()));

        member.setRole(role);
        workspaceMemberRepository.save(member);
    }

    @Transactional
    public void deleteWorkspaceMember(UUID workspaceId, UUID memberId) {
        WorkspaceMemberEntity member = workspaceMemberRepository.findById(new WorkspaceMemberId(workspaceId, memberId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));

        workspaceMemberRepository.delete(member);
    }
}
