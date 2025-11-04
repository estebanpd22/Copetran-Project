package co.unimagdalena.domine.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "seatHolds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatHold {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seat_number", nullable = false)
    private String seatNumber;

    @Column(nullable = false)
    private OffsetDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatHoldStatus status;

    @OneToOne
    @JoinColumn(name = "seat_id")
    private Seat seat;

    @ManyToOne
    @JoinColumn(name = "trip_id")
    private Trip trip;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
