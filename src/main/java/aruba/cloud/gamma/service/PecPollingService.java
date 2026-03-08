package aruba.cloud.gamma.service;

import aruba.cloud.gamma.dto.events.AttachmentToSignEventDTO;
import aruba.cloud.gamma.entity.DocumentWorkflow;
import aruba.cloud.gamma.factory.DocumentWorkflowFactory;
import aruba.cloud.gamma.manager.RepoManager;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PecPollingService {

    private final DocumentWorkflowFactory documentWorkflowFactory;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RepoManager repoManager;

    @Transactional
    public void simulatePecReception(AttachmentToSignEventDTO event) {
        DocumentWorkflow workflow = documentWorkflowFactory.createEntityFromEvent(event);
        repoManager.saveWorkflow(workflow);

        kafkaTemplate.send("attachment-to-sign-topic", event.messageId(), event);
    }

}
