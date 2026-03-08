package aruba.cloud.gamma.manager;

import aruba.cloud.gamma.TestContainersConfig;
import aruba.cloud.gamma.common.enums.WorkflowStatus;
import aruba.cloud.gamma.entity.DocumentWorkflow;
import aruba.cloud.gamma.entity.PecFilter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
        Assertions.assertNotNull(savedFilter.getId(), "L'ID non deve essere null");

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

    @Test
    public void testDocumentWorkfolwOperations(){
        String messageId = UUID.randomUUID().toString();

        DocumentWorkflow workflow = DocumentWorkflow.builder()
                .messageId(messageId)
                .tenantId("tenant-1")
                .attachmentName("contratto.pdf")
                .status(WorkflowStatus.RECIEVED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        DocumentWorkflow savedWorkflow = repoManager.saveWorkflow(workflow);
        Assertions.assertNotNull(savedWorkflow);
        Assertions.assertNotNull(savedWorkflow.getId(), "L'ID non deve essere null");

        DocumentWorkflow retrivedWorkflow = repoManager.findWorkflowByMessageId(messageId).orElse(null);
        Assertions.assertNotNull(retrivedWorkflow);
        Assertions.assertEquals("tenant-1", retrivedWorkflow.getTenantId());
        Assertions.assertEquals(WorkflowStatus.RECIEVED, retrivedWorkflow.getStatus());

        retrivedWorkflow.setStatus(WorkflowStatus.CONSERVED);
        retrivedWorkflow.setConservationId("CONS_123");
        repoManager.saveWorkflow(retrivedWorkflow);

        DocumentWorkflow updatedWorkflow = repoManager.findWorkflowByMessageId(messageId).orElse(null);
        Assertions.assertNotNull(updatedWorkflow);
        Assertions.assertEquals(WorkflowStatus.CONSERVED, updatedWorkflow.getStatus());
        Assertions.assertEquals("CONS_123", updatedWorkflow.getConservationId());
    }

}
