package aruba.cloud.gamma.integration;

import aruba.cloud.gamma.config.ArubaApiProperties;
import aruba.cloud.gamma.dto.signature.ArubaSignResponseDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArubaSignatureGateway {

    private final ArubaApiProperties arubaApiProperties;
    private final RestClient restClient;

    @CircuitBreaker(name = "aruba-sign-circuit-breaker",
                    fallbackMethod = "arubaSignFallback")
    public ArubaSignResponseDTO callArubaSign(String base64Content) {
        return restClient.post()
                .uri(arubaApiProperties.signPath())
                .body(base64Content)
                .retrieve()
                .body(ArubaSignResponseDTO.class);
    }
    
    public ArubaSignResponseDTO arubaSignFallback(String base64Content, Throwable t) {
        log.warn("FALLBACK FIRMA ATTIVATO: Aruba non è raggiungibile: {}", t.getMessage());
        return new ArubaSignResponseDTO("fallback", "FALLBACK-SIGN-ID");
    }
}
