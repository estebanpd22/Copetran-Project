package co.unimagdalena.domine.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "trips")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Trip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trip_id")
    private Long id;

    @Column(nullable = false, name = "date")
    private LocalDate date;

    @Column(nullable = false, name = "departure_at")
    private OffsetDateTime departureAt;

    @Column(nullable = false, name = "arrival_at")
    private OffsetDateTime arrivalAt;

    @Column(nullable = false, name = "status")
    private TripStatus status;

    @ManyToOne
    @JoinColumn(name = "route_id")
    private Route route;

    @ManyToOne
    @JoinColumn(name = "bus_id")
    private Bus bus;
}
