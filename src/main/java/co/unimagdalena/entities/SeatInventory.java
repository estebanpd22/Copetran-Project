package co.unimagdalena.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "seatInventories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatInventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private Integer availableSeats;

    @OneToMany(mappedBy = "seatInventory")
    private List<Seat> seats = new ArrayList<>();

}
