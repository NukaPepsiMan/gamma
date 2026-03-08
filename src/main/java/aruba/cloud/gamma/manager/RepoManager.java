package aruba.cloud.gamma.manager;

import aruba.cloud.gamma.entity.PecFilter;
import aruba.cloud.gamma.repository.PecFilterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Transactional
@RequiredArgsConstructor
public class RepoManager {

    private final PecFilterRepository pecFilterRepository;

    public List<PecFilter> findFiltersByTenantId(String tenantId) {
        return pecFilterRepository.findByTenantId(tenantId);
    }

    public PecFilter saveFilter(PecFilter pecFilter) {
        return pecFilterRepository.save(pecFilter);
    }

    public boolean existsFilterById(Long id) {
        return pecFilterRepository.existsById(id);
    }

    public void deleteFilterById(Long id) {
        pecFilterRepository.deleteById(id);
    }
}
