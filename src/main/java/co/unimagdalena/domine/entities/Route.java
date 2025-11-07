package co.unimagdalena.domine.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "routes")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "code")
    private String code;

    @Column(nullable = false, name = "name")
    private String name;

    @Column(nullable = false, name = "origin")
    private String origin;

    @Column(nullable = false, name = "destination")
    private String destination;

    @Column(nullable = false, name = "distance_km")
    private Float distanceKm;

    @Column(nullable = false, name = "duration_min")
    private Float durationMin;

    @OneToMany(mappedBy = "route", fetch = FetchType.LAZY)
    private List<Stop> stops = new ArrayList<>();

    public void addStop(Stop stop) {
        this.stops.add(stop);
        stop.setRoute(this);
    }

    @OneToMany(mappedBy = "route", fetch = FetchType.LAZY)
    private List<Trip>  trips = new ArrayList<>();
    public void addTrip(Trip trip) {
        this.trips.add(trip);
        trip.setRoute(this);
    }

    @OneToMany(mappedBy = "route", fetch = FetchType.LAZY)
    private List<FareRule> fareRules = new ArrayList<>();
    public void addFareRule(FareRule fareRule) {
        this.fareRules.add(fareRule);
        fareRule.setRoute(this);
    }

}
