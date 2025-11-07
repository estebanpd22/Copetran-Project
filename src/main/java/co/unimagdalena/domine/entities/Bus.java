package co.unimagdalena.domine.entities;

import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "buses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bus_id")
    private Long id;

    @Column(nullable = false, unique = true, name = "plate")
    private String plate;

    @Column(nullable = false, name = "capacity")
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "bus_status")
    private BusStatus status;

    @Column(nullable = false, name = "soat")
    private OffsetDateTime soatExpirationDate;

    @OneToMany(mappedBy = "bus", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @Builder.Default
    private List<Trip> trips =  new ArrayList<>();
    public void addTrip(Trip trip) {
        this.trips.add(trip);
        trip.setBus(this);
    }

    @OneToMany(mappedBy = "bus",fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @Builder.Default
    private List<Seat> seats =  new ArrayList<>();
    public void addSeat(Seat seat) {
        this.seats.add(seat);
        seat.setBus(this);
    }

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private Set<Amenity> amenities = new HashSet<>();
}
