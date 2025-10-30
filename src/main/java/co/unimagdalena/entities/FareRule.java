package co.unimagdalena.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "fareRules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FareRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer basePrice;

    @Column(nullable = false)
    private boolean isDynamicPricing;

    @ManyToOne
    @JoinColumn(name = "route_id")
    private Route route;

    @ManyToOne
    @JoinColumn(name = "from_stop_id", referencedColumnName = "stop_id")
    private Stop fromStop;

    @ManyToOne
    @JoinColumn(name = "to_stop_id", referencedColumnName = "stop_id")
    private Stop toStop;
}
