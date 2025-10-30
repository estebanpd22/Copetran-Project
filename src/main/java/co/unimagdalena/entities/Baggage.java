package co.unimagdalena.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "baggages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Baggage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Float weightKg;

    @Column(nullable = false)
    private BigDecimal fee;

    private String tagCode;

    @ManyToOne
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;
}
