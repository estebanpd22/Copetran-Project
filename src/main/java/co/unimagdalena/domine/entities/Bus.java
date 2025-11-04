package co.unimagdalena.domine.entities;

import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

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
    private Long id;

    @Column(nullable = false, unique = true)
    private String plate;

    @Column(nullable = false)
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BusStatus status;

    @OneToMany(mappedBy = "bus", fetch = FetchType.LAZY)
    private List<Trip> trips;
    public void addTrip(Trip trip) {
        this.trips.add(trip);
        trip.setBus(this);
    }

    @OneToMany(mappedBy = "bus",fetch = FetchType.LAZY)
    private List<Seat> seats;
    public void addSeat(Seat seat) {
        this.seats.add(seat);
        seat.setBus(this);
    }

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Set<Amenity> amenities = new HashSet<>();
}
