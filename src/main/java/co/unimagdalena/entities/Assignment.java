package co.unimagdalena.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private boolean checkListOk;

    @Column(nullable = false)
    private LocalDate  assignedAt;

    @OneToOne
    @JoinColumn(name = "trip_id")
    private Trip trip;

    @ManyToOne
    @JoinColumn(name = "driver_id", referencedColumnName = "user_id")
    private User driver;

    @ManyToOne
    @JoinColumn(name = "dispatcher_id", referencedColumnName = "user_id")
    private User dispatcher;
}
