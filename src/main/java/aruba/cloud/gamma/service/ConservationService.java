package aruba.cloud.gamma.service;

import aruba.cloud.gamma.common.enums.WorkflowStatus;
import aruba.cloud.gamma.config.kafka.KafkaTopicConstants;
import aruba.cloud.gamma.dto.events.DocumentSignedEventDTO;
import aruba.cloud.gamma.dto.signature.ArubaSignResponseDTO;
import aruba.cloud.gamma.integration.ArubaConservationGateway;
import aruba.cloud.gamma.manager.RepoManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConservationService {

    private final ArubaConservationGateway conservationGateway;
    private final RepoManager repoManager;

    @KafkaListener(topics = KafkaTopicConstants.DOCUMENT_SIGNED_TOPIC, groupId = "conservation-group")
    public void processDocumentToConservation(DocumentSignedEventDTO event) {
        log.info("Processing attachment for signature: {}", event.signedDocumentId());

        try {
            repoManager.findWorkflowByMessageId(event.messageId()).ifPresent(wf -> {
                wf.setStatus(WorkflowStatus.CONSERVATION_PENDING);
                wf.setUpdatedAt(LocalDateTime.now());
                repoManager.saveWorkflow(wf);
            });

            ArubaSignResponseDTO response = conservationGateway.callArubaConserve(event);

            log.info("Documento mandato in conservazione con successo! Esito Aruba: {}", response);

            repoManager.findWorkflowByMessageId(event.messageId()).ifPresent(wf -> {
                wf.setStatus(WorkflowStatus.CONSERVED);
                wf.setConservationId(response.documentId());
                wf.setUpdatedAt(LocalDateTime.now());
                repoManager.saveWorkflow(wf);
            });
        } catch (Exception e) {
            log.error("Errore durante la conservazione del documento: {}", e.getMessage(), e);
            repoManager.findWorkflowByMessageId(event.messageId()).ifPresent(wf -> {
                wf.setStatus(WorkflowStatus.ERROR);
                wf.setUpdatedAt(LocalDateTime.now());
                repoManager.saveWorkflow(wf);
            });
            throw e;
        }
    }

}

