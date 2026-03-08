package aruba.cloud.gamma.dto.events;

import lombok.Builder;

@Builder
public record AttachmentToSignEventDTO(
        String messageId,
        String tenantId,
        String attachmentName,
        String base64content
) {
}
