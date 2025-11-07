package co.unimagdalena.domine.entities;

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

    @Column(nullable = false, unique = true, name = "code")
    private String code;

    @Column(nullable = false, length = 120, name = "sender_name")
    private String senderName;

    @Column(nullable = false, length = 120, name = "receiver_name")
    private String receiverName;

    @Column(nullable = false, length = 10, name = "sender_phone")
    private String senderPhone;

    @Column(nullable = false, length = 10, name = "receiver_phone")
    private String receiverPhone;

    @Column(nullable = false, name = "price")
    private BigDecimal price;

    @Column(nullable = false, name = "parcel_status")
    @Enumerated(EnumType.STRING)
    private ParcelStatus status;

    @Column(name = "proof_photo_url")
    private String proofPhotoUrl;

    @Column(name = "delivery_otp")
    private String deliveryOTP;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = true,
            foreignKey = @ForeignKey(name = "fk_parcel_trip"))
    private Trip trip;

    @ManyToOne
    @JoinColumn(name = "from_stop_id", referencedColumnName = "stop_id")
    private Stop fromStop;

    @ManyToOne
    @JoinColumn(name = "to_stop_id", referencedColumnName = "stop_id")
    private Stop toStop;
}