package aruba.cloud.gamma.manager;

import aruba.cloud.gamma.entity.DocumentWorkflow;
import aruba.cloud.gamma.entity.PecFilter;
import aruba.cloud.gamma.repository.DocumentWorkflowRepository;
import aruba.cloud.gamma.repository.PecFilterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Transactional
@RequiredArgsConstructor
public class RepoManager {

    private final PecFilterRepository pecFilterRepository;
    private final DocumentWorkflowRepository documentWorkflowRepository;

    // Metodi PecFilter
    public List<PecFilter> findFiltersByTenantId(String tenantId) {
        return pecFilterRepository.findByTenantId(tenantId);
    }

    public PecFilter saveFilter(PecFilter pecFilter) {
        return pecFilterRepository.save(pecFilter);
    }

    public boolean existsFilterById(Long id) {
        return pecFilterRepository.existsById(id);
    }

    public void deleteFilterById(Long id) {
        pecFilterRepository.deleteById(id);
    }

    // Metodi DocumentWorkflow

    public Optional<DocumentWorkflow> findWorkflowByMessageId(String messageId) {
        return documentWorkflowRepository.findByMessageId(messageId);
    }

    public DocumentWorkflow saveWorkflow(DocumentWorkflow workflow) {
        return documentWorkflowRepository.save(workflow);
    }

}
