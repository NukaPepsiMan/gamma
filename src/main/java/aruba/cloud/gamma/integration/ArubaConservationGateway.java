package aruba.cloud.gamma.integration;

import aruba.cloud.gamma.config.ArubaApiProperties;
import aruba.cloud.gamma.dto.events.DocumentSignedEventDTO;
import aruba.cloud.gamma.dto.signature.ArubaSignResponseDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArubaConservationGateway {

    private final ArubaApiProperties arubaApiProperties;
    private final RestClient restClient;

    @CircuitBreaker(name = "aruba-conservation-circuit-breaker",
            fallbackMethod = "arubaConservationFallback")
    public ArubaSignResponseDTO callArubaConserve(DocumentSignedEventDTO event) {
        return restClient.post()
                .uri(arubaApiProperties.conservePath())
                .body(event)
                .retrieve()
                .body(ArubaSignResponseDTO.class);
    }

    public ArubaSignResponseDTO arubaConservationFallback(DocumentSignedEventDTO event, Throwable t) {
        log.warn("FALLBACK CONSERVAZIONE ATTIVATO: Aruba non è raggiungibile: {}", t.getMessage());
        return new ArubaSignResponseDTO("fallback", "FALLBACK-CONS-ID");
    }
}
