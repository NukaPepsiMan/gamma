package aruba.cloud.gamma.integration.kafka;

import aruba.cloud.gamma.TestContainersConfig;
import aruba.cloud.gamma.dto.events.AttachmentToSignEventDTO;
import aruba.cloud.gamma.entity.DocumentWorkflow;
import aruba.cloud.gamma.manager.RepoManager;
import aruba.cloud.gamma.service.PecPollingService;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@ActiveProfiles("test")
@SpringBootTest
@Import(TestContainersConfig.class)
public class KafkaIntegrationTest {

    @Autowired
    private PecPollingService pecPollingService;

    @Autowired
    private RepoManager repoManager;

    @RegisterExtension
    private static final WireMockExtension wireMockServer = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().dynamicPort())
            .build();

    private static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("aruba.api.base-url", wireMockServer::baseUrl);
    }

    @Test
    public void testPecPollingService() {

        String messageId = UUID.randomUUID().toString();
        AttachmentToSignEventDTO event = AttachmentToSignEventDTO.builder()
                .messageId(messageId)
                .tenantId("tenat-test")
                .attachmentName("contratto.pdf")
                .base64content("fafhahfoasdfh")
                .build();

        pecPollingService.simulatePecReception(event);

        Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Optional<DocumentWorkflow> workflow = repoManager.findWorkflowByMessageId(messageId);
            Assertions.assertTrue(workflow.isPresent(), "il documento in polling deve esistere sul db");
        });
    }
}
