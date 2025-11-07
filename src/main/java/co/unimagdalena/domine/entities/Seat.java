package co.unimagdalena.domine.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "seats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "price")
    private BigDecimal price;

    @Column(nullable = false, name = "number")
    private Integer number;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "type")
    private SeatType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status;

    @ManyToOne
    @JoinColumn(name = "bus_id",foreignKey = @ForeignKey(name = "fk_seat_bus"))
    private Bus bus;
}