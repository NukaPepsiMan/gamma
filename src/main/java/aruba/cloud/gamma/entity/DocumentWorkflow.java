package aruba.cloud.gamma.entity;

import aruba.cloud.gamma.common.enums.WorkflowStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "document_workflows")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class DocumentWorkflow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String messageId;

    private String tenantId;

    private String attachmentName;

    @Enumerated(EnumType.STRING)
    private WorkflowStatus status;

    private String signedDocument;

    private String conservationId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
