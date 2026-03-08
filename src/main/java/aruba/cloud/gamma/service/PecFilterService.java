package aruba.cloud.gamma.service;

import aruba.cloud.gamma.dto.pec.PecFilterDTO;
import aruba.cloud.gamma.entity.PecFilter;
import aruba.cloud.gamma.factory.PecFilterFactory;
import aruba.cloud.gamma.manager.RepoManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PecFilterService {

    private final RepoManager repoManager;
    private final PecFilterFactory filterFactory;

    public List<PecFilterDTO> getFiltersByTenant(String tenant) {
        List<PecFilter> filters = repoManager.findFiltersByTenantId(tenant);
        return filters.stream()
                .map(filterFactory::createDTO)
                .collect(Collectors.toList());
    }


    public PecFilterDTO createFilter(PecFilterDTO dto) {
        PecFilter filter = filterFactory.createEntity(dto);
        PecFilter savedFilter = repoManager.saveFilter(filter);
        return filterFactory.createDTO(savedFilter);
    }

    public boolean deleteFilter(Long id){
        if (repoManager.existsFilterById(id)) {
            repoManager.deleteFilterById(id);
            return true;
        } else return false;
    }


}
