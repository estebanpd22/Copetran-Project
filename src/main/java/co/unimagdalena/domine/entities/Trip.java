package co.unimagdalena.domine.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trips")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Trip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trip_id")
    private Long id;

    @Column(nullable = false, name = "date")
    private LocalDate date;

    @Column(nullable = false, name = "departure_at")
    private OffsetDateTime departureAt;

    @Column(nullable = false, name = "arrival_at")
    private OffsetDateTime arrivalAt;

    @Column(nullable = false, name = "status")
    @Enumerated(EnumType.STRING)
    private TripStatus status;

    @Column(name = "boarding_status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private BoardingStatus boardingStatus = BoardingStatus.NOT_STARTED;

    @Column(name = "actual_departure_at")
    private OffsetDateTime actualDepartureAt;

    @ManyToOne
    @JoinColumn(name = "route_id")
    private Route route;

    @ManyToOne
    @JoinColumn(name = "bus_id")
    private Bus bus;

    @OneToOne(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private Checklist checklist;

    @OneToMany(mappedBy = "trip",  fetch = FetchType.LAZY)
    private List<SeatHold> seatHolds= new ArrayList<>();

    public void addSeatHold(SeatHold seatHold) {
        this.seatHolds.add(seatHold);
        seatHold.setTrip(this);
    }

    @OneToMany(mappedBy = "trip",fetch = FetchType.LAZY)
    private List<Ticket> tickets= new ArrayList<>();

    public void addTicket(Ticket ticket) {
        this.tickets.add(ticket);
        ticket.setTrip(this);
    }

    @OneToMany(mappedBy = "trip", fetch = FetchType.LAZY)
    private List<Parcel> parcels= new ArrayList<>();

    public void addParcel(Parcel parcel) {
        this.parcels.add(parcel);
        parcel.setTrip(this);
    }
}
