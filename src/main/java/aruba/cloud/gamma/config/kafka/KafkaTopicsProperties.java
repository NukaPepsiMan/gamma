package aruba.cloud.gamma.config.kafka;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kafka.topic")
public record KafkaTopicsProperties(
        String attachmentToSign,
        String documentSigned
) {

}
