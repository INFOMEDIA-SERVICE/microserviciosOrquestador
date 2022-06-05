package infomediaservice.vuplaformserver.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "Validations")
public class Validation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "HttpCode", nullable = false)
    private Integer httpCode;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "Message", nullable = false)
    private String message;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "RequestType", nullable = false)
    private String requestType;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "TransactionId", nullable = false)
    private Transaction transaction;

}
