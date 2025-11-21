package co.unimagdalena.domine.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, name = "full_name")
    private String fullName;

    @Column(nullable = false, unique = true, name = "email")
    private String email;

    @Column(nullable = false, name = "phone")
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "role")
    private UserRole role;

    @Column(nullable = false, name = "password_hash")
    private String passwordHash;

    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "status")
    private UserStatus status;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Passenger> passengers = new ArrayList<>();

    public void addPassenger(Passenger passenger) {
        this.passengers.add(passenger);
        passenger.setUser(this);
    }

    @OneToMany(mappedBy = "driver",  fetch = FetchType.LAZY)
    @Builder.Default
    private List<Assignment> assignmentsAsDriver =  new ArrayList<>();

    public void addAssignmentAsDriver(Assignment assignment) {
        this.assignmentsAsDriver.add(assignment);
        assignment.setDriver(this);
    }

    @OneToMany(mappedBy = "dispatcher", fetch = FetchType.LAZY)
    private List<Assignment> assignmentsAsDispatcher = new ArrayList<>();

    public void addAssignmentAsDispatcher(Assignment assignment) {
        this.assignmentsAsDispatcher.add(assignment);
        assignment.setDispatcher(this);
    }

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<SeatHold> seatHolds = new ArrayList<>();

    public void addSeatHold(SeatHold seatHold) {
        this.seatHolds.add(seatHold);
        seatHold.setUser(this);
    }

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Purchase> purchases = new ArrayList<>();

    public void addPurchase(Purchase purchase) {
        this.purchases.add(purchase);
        purchase.setUser(this);
    }
}