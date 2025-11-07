package co.unimagdalena.domine.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stops")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Stop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stop_id")
    private Long id;

    @Column(nullable = false, name = "name")
    private String name;

    @Column(nullable = false, name = "order")
    private Integer order;

    @Column(nullable = false, name = "latitude")
    private double latitude;

    @Column(nullable = false, name = "longitud")
    private double longitude;

    @ManyToOne
    @JoinColumn(name = "route_id")
    private Route route;

    // Reglas donde esta parada es el origen (from_stop)
    @OneToMany(mappedBy = "fromStop", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<FareRule> fareRulesFrom = new ArrayList<>();
    public void addFareRuleFrom(FareRule fareRule) {
        this.fareRulesFrom.add(fareRule);
        fareRule.setFromStop(this);
    }

    // Reglas donde esta parada es el destino (to_stop)
    @OneToMany(mappedBy = "toStop", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<FareRule> fareRulesTo = new ArrayList<>();
    public void addFareRuleTo(FareRule fareRule) {
        this.fareRulesTo.add(fareRule);
        fareRule.setToStop(this);
    }
}
