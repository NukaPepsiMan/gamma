package aruba.cloud.gamma.config.kafka;

public interface KafkaTopicConstants {
    String ATTACHMENT_TO_SIGN_TOPIC = "${kafka.topic.attachment-to-sign}";
    String  DOCUMENT_SIGNED_TOPIC = "${kafka.topic.document-signed}";
}
