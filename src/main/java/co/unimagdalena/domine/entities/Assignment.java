package co.unimagdalena.domine.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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
    @Column(name = "assinment_id")
    private Long id;

    @Column(nullable = false, name = "check_list_ok")
    private boolean checkListOk;

    @Column(nullable = false, name = "assigned_at")
    private LocalDateTime assignedAt;

    @OneToOne
    @JoinColumn(name = "trip_id")
    private Trip trip;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "driver_id", referencedColumnName = "user_id")
    private User driver;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "dispatcher_id", referencedColumnName = "user_id")
    private User dispatcher;
}