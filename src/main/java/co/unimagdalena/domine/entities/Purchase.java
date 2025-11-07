package co.unimagdalena.domine.entities;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.boot.autoconfigure.web.WebProperties;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Purchase {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "purchase_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "payment_method")
    private PaymentMethod paymentMethod;

    @Column(nullable = false, name = "total_amount")
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "payment_status")
    private PaymentStatus paymentStatus;

    @Column(nullable = false, name = "created_at")
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "purchase", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Column(nullable = false)
    @Builder.Default
    private List<Ticket> tickets =  new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}