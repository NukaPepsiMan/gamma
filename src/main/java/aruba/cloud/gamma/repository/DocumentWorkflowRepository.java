package aruba.cloud.gamma.repository;

import aruba.cloud.gamma.entity.DocumentWorkflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentWorkflowRepository extends JpaRepository<DocumentWorkflow, Long> {
    Optional<DocumentWorkflow> findByMessageId(String messageId);
}
