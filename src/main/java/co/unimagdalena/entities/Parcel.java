package co.unimagdalena.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "parcels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Parcel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false, length = 120)
    private String senderName;

    @Column(nullable = false, length = 120)
    private String receiverName;

    @Column(nullable = false, length = 10)
    private String senderPhone;

    @Column(nullable = false, length = 10)
    private String receiverPhone;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ParcelStatus status;

    private String proofPhotoUrl;

    private String deliveryOTP;

    @ManyToOne
    @JoinColumn(name = "from_stop_id", referencedColumnName = "stop_id")
    private Stop fromStop;

    @ManyToOne
    @JoinColumn(name = "to_stop_id", referencedColumnName = "stop_id")
    private Stop toStop;
}
