package co.unimagdalena.domine.entities;

import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "fare_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FareRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fare_rule_id")
    private Long id;

    @Column(nullable = false, name = "base_price")
    private BigDecimal basePrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "dynamic_pricing")
    private DynamicPricing dynamicPricing;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Double> discounts = new HashMap<>();

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