package co.unimagdalena.domine.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "checklists")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Checklist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "checklist_id")
    private Long id;

    @OneToOne
    @JoinColumn(name = "trip_id", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_checklist_trip"))
    private Trip trip;

    @Column(name = "fuel_check", nullable = false)
    private Boolean fuelCheck = false;

    @Column(name = "tire_check", nullable = false)
    private Boolean tireCheck = false;

    @Column(name = "brake_check", nullable = false)
    private Boolean brakeCheck = false;

    @Column(name = "lights_check", nullable = false)
    private Boolean lightsCheck = false;

    @Column(name = "emergency_equipment_check", nullable = false)
    private Boolean emergencyEquipmentCheck = false;

    @Column(name = "documents_check", nullable = false)
    private Boolean documentsCheck = false;

    @Column(name = "cleanliness_check", nullable = false)
    private Boolean cleanlinessCheck = false;

    @Column(name = "seats_check", nullable = false)
    private Boolean seatsCheck = false;

    @Column(name = "completed", nullable = false)
    private Boolean completed = false;

    @ManyToOne
    @JoinColumn(name = "completed_by_user_id",
            foreignKey = @ForeignKey(name = "fk_checklist_user"))
    private User completedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "notes", length = 500)
    private String notes;

    public boolean isAllChecksComplete() {
        return Boolean.TRUE.equals(fuelCheck) &&
               Boolean.TRUE.equals(tireCheck) &&
               Boolean.TRUE.equals(brakeCheck) &&
               Boolean.TRUE.equals(lightsCheck) &&
               Boolean.TRUE.equals(emergencyEquipmentCheck) &&
               Boolean.TRUE.equals(documentsCheck) &&
               Boolean.TRUE.equals(cleanlinessCheck) &&
               Boolean.TRUE.equals(seatsCheck);
    }
}
