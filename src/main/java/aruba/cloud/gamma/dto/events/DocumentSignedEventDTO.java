package aruba.cloud.gamma.dto.events;

import lombok.Builder;

@Builder
public record DocumentSignedEventDTO(
        String messageId,
        String tenantId,
        String signedDocumentId
) {
}
