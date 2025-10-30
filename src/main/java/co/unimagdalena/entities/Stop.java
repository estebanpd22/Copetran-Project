package co.unimagdalena.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stops")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Stop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer order;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @ManyToOne
    @JoinColumn(name = "route_id")
    private Route route;
}
