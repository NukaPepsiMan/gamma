package aruba.cloud.gamma.service;


import aruba.cloud.gamma.common.enums.WorkflowStatus;
import aruba.cloud.gamma.config.kafka.KafkaTopicConstants;
import aruba.cloud.gamma.config.kafka.KafkaTopicsProperties;
import aruba.cloud.gamma.dto.events.AttachmentToSignEventDTO;
import aruba.cloud.gamma.dto.events.DocumentSignedEventDTO;
import aruba.cloud.gamma.dto.signature.ArubaSignResponseDTO;
import aruba.cloud.gamma.integration.ArubaSignatureGateway;
import aruba.cloud.gamma.manager.RepoManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignatureService {

    private final KafkaTopicsProperties kafkaTopicsProperties;
    private final ArubaSignatureGateway arubaSignatureGateway;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RepoManager repoManager;

    @KafkaListener(topics = KafkaTopicConstants.ATTACHMENT_TO_SIGN_TOPIC, groupId = "signature-group")
    public void processAttachmentForSignature(AttachmentToSignEventDTO event){
        log.info("Elaborazione allegati da firmare: {}", event.attachmentName());

        try {
            repoManager.findWorkflowByMessageId(event.messageId()).ifPresent(wf -> {
                wf.setStatus(WorkflowStatus.SIGNATURE_PENDING);
                wf.setUpdatedAt(LocalDateTime.now());
                repoManager.saveWorkflow(wf);
            });

            ArubaSignResponseDTO response = arubaSignatureGateway.callArubaSign(event.base64content());
            String signedDocId = response.documentId();
            log.info("Documento firmato con successo! ID Aruba: {}", signedDocId);

            repoManager.findWorkflowByMessageId(event.messageId()).ifPresent(wf -> {
                wf.setStatus(WorkflowStatus.SIGNED);
                wf.setSignedDocumentId(signedDocId);
                wf.setUpdatedAt(LocalDateTime.now());
                repoManager.saveWorkflow(wf);
            });

            DocumentSignedEventDTO documentSignedEventDTO = DocumentSignedEventDTO.builder()
                    .messageId(event.messageId())
                    .tenantId(event.tenantId())
                    .signedDocumentId(signedDocId)
                    .build();
            kafkaTemplate.send(kafkaTopicsProperties.documentSigned(), event.messageId(), documentSignedEventDTO);

        } catch (Exception e) {
            log.error("Errore durante la firma del documento. Messaggio non inviato in conservazione: {}", e.getMessage(), e);
            repoManager.findWorkflowByMessageId(event.messageId()).ifPresent(wf -> {
                wf.setStatus(WorkflowStatus.ERROR);
                wf.setUpdatedAt(LocalDateTime.now());
                repoManager.saveWorkflow(wf);
            });
            throw e;
        }
    }
}
