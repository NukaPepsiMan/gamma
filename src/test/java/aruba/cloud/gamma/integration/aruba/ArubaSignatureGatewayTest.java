package aruba.cloud.gamma.integration.aruba;


import aruba.cloud.gamma.config.ArubaApiProperties;
import aruba.cloud.gamma.config.oauth2.OAuth2RestClientConfig;
import aruba.cloud.gamma.dto.signature.ArubaSignResponseDTO;
import aruba.cloud.gamma.integration.ArubaSignatureGateway;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.springboot3.circuitbreaker.autoconfigure.CircuitBreakerAutoConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ActiveProfiles("gateway")
@EnableConfigurationProperties(ArubaApiProperties.class)
@SpringBootTest(classes = {ArubaSignatureGateway.class, OAuth2RestClientConfig.class})
@ImportAutoConfiguration({
        AopAutoConfiguration.class,
        HttpMessageConvertersAutoConfiguration.class,
        RestClientAutoConfiguration.class,
        JacksonAutoConfiguration.class,
        CircuitBreakerAutoConfiguration.class
})
public class ArubaSignatureGatewayTest {

    @MockitoBean
    private OAuth2AuthorizedClientManager authorizedClientManager;

    @RegisterExtension
    private static final WireMockExtension wireMockServer = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    private static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("aruba.api.base-url", wireMockServer::baseUrl);
    }

    @Autowired
    private ArubaSignatureGateway arubaSignatureGateway;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @AfterEach
    public void cleanup() {
        circuitBreakerRegistry.circuitBreaker("aruba-sign-circuit-breaker").transitionToClosedState();
        wireMockServer.resetAll();
    }

    @Test
    public void testCallArubaSign_Success() {
        // Setup WireMock per rispondere 200 OK
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api/aruba/sign"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\":\"success\", \"documentId\":\"REAL-DOC-123\"}")));

        // Chiamata al metodo
        ArubaSignResponseDTO response = arubaSignatureGateway.callArubaSign("Base64Test");

        // Asserzione: verifichiamo che il client abbia mappato correttamente l'ID reale
        Assertions.assertEquals("success", response.status());
        Assertions.assertEquals("REAL-DOC-123", response.documentId());
    }

    @Test
    public void testCallArubaSign_Fallback_OnServerError() {
        // Setup WireMock per rispondere 500 Errore Server
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api/aruba/sign"))
                .willReturn(WireMock.aResponse()
                        .withStatus(500)
                        .withBody("Internal Server Error")));

        // Chiamata al metodo. Qui l'eccezione 500 verrà catturata dal Fallback!
        ArubaSignResponseDTO response = arubaSignatureGateway.callArubaSign("Base64Test");

        // Asserzione: verifichiamo che sia stato restituito l'ID di fallback
        Assertions.assertEquals("fallback", response.status());
        Assertions.assertEquals("FALLBACK-SIGN-ID", response.documentId());
    }
}
