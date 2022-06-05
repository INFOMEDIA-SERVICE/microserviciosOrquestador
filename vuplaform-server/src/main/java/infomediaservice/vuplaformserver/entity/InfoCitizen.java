package infomediaservice.vuplaformserver.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "InfoCitizens")
public class InfoCitizen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Integer id;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "Identification")
    private String identification;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "DocumentType")
    private String documentType;

    @Column(name = "Success", nullable = false)
    private Boolean success = false;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "ResultCode", nullable = false)
    private String resultCode;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "Response")
    private String response;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "ConsultationDate")
    private String consultationDate;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "Name")
    private String name;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "FirstLastName")
    private String firstLastName;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "SecondLastName")
    private String secondLastName;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "FullName")
    private String fullName;

    @Column(name = "Validated")
    private Boolean validated;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "IdentificationState")
    private String identificationState;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "ExpeditionDate")
    private String expeditionDate;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "City")
    private String city;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "Departament")
    private String departament;

    @ManyToOne(optional = false)
    @JoinColumn(name = "VuCustomerId", nullable = false)
    private VuCustomer vuCustomer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "TransactionId", nullable = false)
    private Transaction transaction;

    @ManyToOne
    @JoinColumn(name = "ValidationId")
    private Validation validation;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "Nacionalidad")
    private String nacionalidad;
}
