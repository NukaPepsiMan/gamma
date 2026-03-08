package aruba.cloud.gamma.factory;

import aruba.cloud.gamma.dto.events.AttachmentToSignEventDTO;
import aruba.cloud.gamma.entity.DocumentWorkflow;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DocumentWorkflowFactory {

    public DocumentWorkflow createEntityFromEvent(AttachmentToSignEventDTO event) {
        return DocumentWorkflow.builder()
                .messageId(event.messageId())
                .tenantId(event.tenantId())
                .attachmentName(event.attachmentName())
                .status("RECEIVED")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
