package co.unimagdalena.domine.entities;

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

    @Column(nullable = false, name = "weight_kg")
    private Float weightKg;

    @Column(nullable = false, name = "fee")
    private BigDecimal fee;

    @Column(name = "tag_code")
    private String tagCode;

    @ManyToOne
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;
}
