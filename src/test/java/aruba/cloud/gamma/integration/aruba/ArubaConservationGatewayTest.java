package aruba.cloud.gamma.integration.aruba;

import aruba.cloud.gamma.config.ArubaApiProperties;
import aruba.cloud.gamma.config.oauth2.OAuth2RestClientConfig;
import aruba.cloud.gamma.dto.events.DocumentSignedEventDTO;
import aruba.cloud.gamma.dto.signature.ArubaSignResponseDTO;
import aruba.cloud.gamma.integration.ArubaConservationGateway;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.springboot3.circuitbreaker.autoconfigure.CircuitBreakerAutoConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@ActiveProfiles("gateway")
@EnableConfigurationProperties(ArubaApiProperties.class)
@SpringBootTest(classes = {ArubaConservationGateway.class, OAuth2RestClientConfig.class})
@ImportAutoConfiguration({
        AopAutoConfiguration.class,
        HttpMessageConvertersAutoConfiguration.class,
        RestClientAutoConfiguration.class,
        JacksonAutoConfiguration.class,
        CircuitBreakerAutoConfiguration.class
})
public class ArubaConservationGatewayTest {

    @MockitoBean
    private OAuth2AuthorizedClientManager authorizedClientManager;

    @RegisterExtension
    private static final WireMockExtension wireMockServer = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().dynamicPort())
            .build();


    @Autowired
    private ArubaConservationGateway arubaConservationGateway;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @DynamicPropertySource
    private static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("aruba.api.base-url", wireMockServer::baseUrl);
    }

    @AfterEach
    public void cleanup() {
        circuitBreakerRegistry.circuitBreaker("aruba-conservation-circuit-breaker").transitionToClosedState();
        wireMockServer.resetAll();
    }

    @Test
    public void testArubaConservation_Fallback_Success() {
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api/aruba/conserve"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\":\"success\", \"documentId\":\"CONS-999\"}")));

        DocumentSignedEventDTO event = DocumentSignedEventDTO.builder()
                .messageId("msg-1")
                .tenantId("tenant-1")
                .signedDocumentId("DOC-123")
                .build();
        ArubaSignResponseDTO response = arubaConservationGateway.callArubaConserve(event);

        Assertions.assertNotNull(response);
        Assertions.assertEquals("success", response.status());
        Assertions.assertEquals("CONS-999", response.documentId());
    }

    @Test
    public void testArubaConservation_Fallback_Fallback_OnServerError() {
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api/aruba/conserve"))
                .willReturn(WireMock.aResponse()
                        .withStatus(500)
                        .withBody("Internal Server Error")));

        DocumentSignedEventDTO event = DocumentSignedEventDTO.builder()
                .messageId("msg-2")
                .tenantId("tenant-1")
                .signedDocumentId("DOC-456")
                .build();
        ArubaSignResponseDTO response = arubaConservationGateway.callArubaConserve(event);

        Assertions.assertNotNull(response);
        Assertions.assertEquals("fallback", response.status());
        Assertions.assertEquals("FALLBACK-CONS-ID", response.documentId());
    }
}