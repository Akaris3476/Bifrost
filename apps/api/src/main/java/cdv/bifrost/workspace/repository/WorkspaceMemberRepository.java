package cdv.bifrost.workspace.repository;

import cdv.bifrost.auth.model.entity.UserEntity;
import cdv.bifrost.workspace.model.entity.WorkspaceMemberEntity;
import cdv.bifrost.workspace.model.entity.WorkspaceMemberId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMemberEntity, WorkspaceMemberId> {

    boolean existsByWorkspace_IdAndUser_Id(UUID workspaceId, UUID userId);

    Optional<WorkspaceMemberEntity> findByWorkspace_IdAndUser_Id(UUID workspaceId, UUID userId);

    List<WorkspaceMemberEntity> findByWorkspace_Id(UUID workspaceId);

    @EntityGraph(attributePaths = {"workspace"})
    List<WorkspaceMemberEntity> findByUser_Id(UUID userId);

    UUID user(UserEntity user);
}
