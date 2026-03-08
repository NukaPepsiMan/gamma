package aruba.cloud.gamma.factory;

import aruba.cloud.gamma.dto.pec.PecFilterDTO;
import aruba.cloud.gamma.entity.PecFilter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class PecFilterFactory {

    public PecFilterDTO createDTOFromEntity(@NonNull PecFilter filter) {
        return PecFilterDTO.builder()
                .id(filter.getId())
                .tenantId(filter.getTenantId())
                .mailbox(filter.getMailbox())
                .folder(filter.getFolder())
                .senderFilter(filter.getSenderFilter())
                .subjectFilter(filter.getSubjectFilter())
                .build();
    }

    public PecFilter createEntityFromDTO(@NonNull PecFilterDTO dto) {
        return PecFilter.builder()
                .tenantId(dto.tenantId())
                .mailbox(dto.mailbox())
                .folder(dto.folder())
                .senderFilter(dto.senderFilter())
                .subjectFilter(dto.subjectFilter())
                .build();
    }
}
