package co.unimagdalena.domine.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "purchase_id")
    private Purchase purchase;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "payment_method")
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "payment_status")
    private PaymentStatus status;

    @Column(nullable = false, name = "amount")
    private BigDecimal amount;

    @Column(nullable = false, name = "payment_reference")
    private String paymentReference;

    @Column(nullable = false, name = "confirmation_code")
    private String confirmationCode;

    @Column(nullable = false, name = "confirmed_at")
    private OffsetDateTime confirmedAt;
}
