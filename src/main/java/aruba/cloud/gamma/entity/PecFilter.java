package aruba.cloud.gamma.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pec_filters")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PecFilter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tenantId;

    private String mailbox;

    private String folder;

    private String senderFilter;

    private String subjectFilter;
}
