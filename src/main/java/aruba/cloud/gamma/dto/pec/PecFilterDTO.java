package aruba.cloud.gamma.dto.pec;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record PecFilterDTO(
        Long id,
        @NotBlank(message = "tenantId is mandatory") String tenantId,
        @NotBlank(message = "mailbox is mandatory") String mailbox,
        @NotBlank(message = "folder is mandatory") String folder,
        String senderFilter,
        String subjectFilter
) {
}
