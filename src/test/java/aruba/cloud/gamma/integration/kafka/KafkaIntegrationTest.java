package aruba.cloud.gamma.integration.kafka;

import aruba.cloud.gamma.TestContainersConfig;
import aruba.cloud.gamma.common.enums.WorkflowStatus;
import aruba.cloud.gamma.dto.events.AttachmentToSignEventDTO;
import aruba.cloud.gamma.entity.DocumentWorkflow;
import aruba.cloud.gamma.manager.RepoManager;
import aruba.cloud.gamma.service.PecPollingService;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
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

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @RegisterExtension
    private static final WireMockExtension wireMockServer = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    private static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("aruba.api.base-url", wireMockServer::baseUrl);
        registry.add("spring.security.oauth2.client.provider.aruba.token-uri", () -> wireMockServer.baseUrl() + "/auth/token");
    }

    @BeforeEach
    public void setupWireMock() {
        wireMockServer.stubFor(WireMock.post(WireMock.urlPathEqualTo("/auth/token"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"access_token\":\"mock_token\",\"token_type\":\"Bearer\",\"expires_in\":3600}")));
    }

    @AfterEach
    public void cleanup() {
        // Puliamo il database per i successivi test
        repoManager.deleteAllWorkflows();
        // Resettiamo WireMock
        wireMockServer.resetAll();
        // Resettiamo il Circuit Breaker
        circuitBreakerRegistry.circuitBreaker("aruba-sign-circuit-breaker").transitionToClosedState();
        circuitBreakerRegistry.circuitBreaker("aruba-conservation-circuit-breaker").transitionToClosedState();
    }


    @Test
    public void testPecPollingService() {

        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api/aruba/sign"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\":\"success\", \"documentId\":\"DOC-FIRMATO-123\"}")));

        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api/aruba/conserve"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\":\"success\", \"documentId\":\"CONSERVATO-777\"}")));

        String messageId = UUID.randomUUID().toString();
        AttachmentToSignEventDTO evento = AttachmentToSignEventDTO.builder()
                .messageId(messageId)
                .tenantId("tenant-test")
                .attachmentName("contratto.pdf")
                .base64content("Base64Fake")
                .build();

        pecPollingService.simulatePecReception(evento);

        Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Optional<DocumentWorkflow> workflowOpt = repoManager.findWorkflowByMessageId(messageId);
            Assertions.assertTrue(workflowOpt.isPresent(), "Il record deve esistere nel DB");
            Assertions.assertEquals(
                    WorkflowStatus.CONSERVED,
                    workflowOpt.get().getStatus(),
                    "Lo stato finale deve essere CONSERVED"
            );
            Assertions.assertEquals("DOC-FIRMATO-123", workflowOpt.get().getSignedDocumentId());
            Assertions.assertEquals("CONSERVATO-777", workflowOpt.get().getConservationId());
        });
    }

    @Test
    public void testCircuitBreakerFallback() {
        // 1. diciamo a WireMock di simulare un errore del server Aruba
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api/aruba/sign"))
                .willReturn(WireMock.aResponse()
                        .withStatus(500)
                        .withBody("Internal Server Error da Aruba")));

        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api/aruba/conserve"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\":\"success\", \"documentId\":\"CONSERVATO-777\"}")));

        // 2. Prepariamo un evento PEC
        String messageId = UUID.randomUUID().toString();
        AttachmentToSignEventDTO evento = AttachmentToSignEventDTO.builder()
                .messageId(messageId)
                .tenantId("tenant-test")
                .attachmentName("errore.pdf")
                .base64content("Base64Fake")
                .build();

        // 3. Inviamo su Kafka
        pecPollingService.simulatePecReception(evento);

        Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Optional<DocumentWorkflow> workflowOpt = repoManager.findWorkflowByMessageId(messageId);
            Assertions.assertTrue(workflowOpt.isPresent(), "Il record deve esistere nel DB");

            Assertions.assertEquals(
                    "FALLBACK-SIGN-ID",
                    workflowOpt.get().getSignedDocumentId(),
                    "Deve essere presente l'ID generato dal Fallback"
            );

            Assertions.assertEquals(
                    WorkflowStatus.CONSERVED,
                    workflowOpt.get().getStatus(),
                    "Anche in fallback, il flusso deve terminare con CONSERVED"
            );
            Assertions.assertEquals("CONSERVATO-777", workflowOpt.get().getConservationId());
        });
    }
}
