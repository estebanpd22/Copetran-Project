package co.unimagdalena.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BusStatus status;

    //Foto del bus (OPCIONAL)
    private String photo;

    @OneToOne
    @JoinColumn(name = "seat_inventory_id")
    private SeatInventory seatInventory;

    //Falta modelar en formato JSON
    private Set<Amenity> amenities = new HashSet<>();
}
