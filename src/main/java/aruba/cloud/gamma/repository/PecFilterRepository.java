package aruba.cloud.gamma.repository;

import aruba.cloud.gamma.entity.PecFilter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PecFilterRepository extends JpaRepository<PecFilter, Long> {
    List<PecFilter> findByTenantId(String tenantId);
}
