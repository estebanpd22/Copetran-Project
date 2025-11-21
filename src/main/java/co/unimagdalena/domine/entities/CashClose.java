package co.unimagdalena.domine.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "cash_closes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashClose {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "trip_id")
    private Trip trip;

    private BigDecimal expectedAmount;
    private BigDecimal actualAmount;
    private BigDecimal cashSales;
    private BigDecimal cardSales;
    private BigDecimal transferSales;
    private OffsetDateTime closedAt;

    @Enumerated(EnumType.STRING)
    private CashCloseStatus status;
}
