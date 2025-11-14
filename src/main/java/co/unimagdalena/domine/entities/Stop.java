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

    @Column(nullable = false, name = "stop_order")
    private Integer order;

    @Column(nullable = false, name = "latitude")
    private double latitude;

    @Column(nullable = false, name = "longitud")
    private double longitude;

    @ManyToOne
    @JoinColumn(name = "route_id")
    private Route route;

    @OneToMany(mappedBy = "fromStop", fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<FareRule> fareRulesFrom = new ArrayList<>();
    public void addFareRuleFrom(FareRule fareRule) {
        this.fareRulesFrom.add(fareRule);
        fareRule.setFromStop(this);
    }

    @OneToMany(mappedBy = "toStop", fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<FareRule> fareRulesTo = new ArrayList<>();
    public void addFareRuleTo(FareRule fareRule) {
        this.fareRulesTo.add(fareRule);
        fareRule.setToStop(this);
    }
}