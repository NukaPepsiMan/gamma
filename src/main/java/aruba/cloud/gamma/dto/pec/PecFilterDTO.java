package aruba.cloud.gamma.dto.pec;

import lombok.Builder;

@Builder
public record PecFilterDTO(
        Long id,
        String tenantId,
        String mailbox,
        String folder,
        String senderFilter,
        String subjectFilter
) {
}
