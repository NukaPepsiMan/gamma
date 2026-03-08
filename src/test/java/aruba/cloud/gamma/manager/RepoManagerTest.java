package aruba.cloud.gamma.manager;

import aruba.cloud.gamma.TestContainersConfig;
import aruba.cloud.gamma.entity.PecFilter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({RepoManager.class, TestContainersConfig.class})
public class RepoManagerTest {

    @Autowired
    private RepoManager repoManager;

    @Test
    public void testPecFilterOperations() {
        PecFilter filter = PecFilter.builder()
                .tenantId("tenant-1")
                .mailbox("test@pec.it")
                .folder("inbox")
                .senderFilter("pippo@pec.it")
                .subjectFilter("fattura")
                .build();

        //Controllo il filtro sia salvato correttamente
        PecFilter savedFilter = repoManager.saveFilter(filter);
        Assertions.assertNotNull(savedFilter);
        Assertions.assertNotNull(savedFilter.getId());

        Long filterId = savedFilter.getId();

        //verifico la sua chiamata di esistenza
        Assertions.assertTrue(repoManager.existsFilterById(filterId));

        //recupero i filter per tenant
        List<PecFilter> filters = repoManager.findFiltersByTenantId("tenant-1");
        Assertions.assertEquals(1, filters.size());
        Assertions.assertEquals("test@pec.it", filters.get(0).getMailbox());

        //Verifico la cancellazione
        repoManager.deleteFilterById(filterId);
        Assertions.assertFalse(repoManager.existsFilterById(filterId));
        Assertions.assertTrue(repoManager.findFiltersByTenantId("tenant-1").isEmpty());
    }
}
