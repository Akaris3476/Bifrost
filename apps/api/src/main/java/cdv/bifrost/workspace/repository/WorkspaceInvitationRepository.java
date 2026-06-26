package cdv.bifrost.workspace.repository;

import cdv.bifrost.workspace.model.entity.WorkspaceInvitationEntity;
import cdv.bifrost.workspace.model.enums.WorkspaceInvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkspaceInvitationRepository extends JpaRepository<WorkspaceInvitationEntity, UUID> {

    Optional<WorkspaceInvitationEntity> findByTokenHash(String tokenHash);

    Optional<WorkspaceInvitationEntity> findByWorkspace_IdAndEmailIgnoreCaseAndStatus(
            UUID workspaceId,
            String email,
            WorkspaceInvitationStatus status
    );

    List<WorkspaceInvitationEntity> findByWorkspace_IdOrderByCreatedAtDesc(UUID workspaceId);
}
