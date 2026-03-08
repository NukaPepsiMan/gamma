package aruba.cloud.gamma.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aruba.api")
public record ArubaApiProperties(
        String baseUrl,
        String signPath,
        String conservePath
) {

}
