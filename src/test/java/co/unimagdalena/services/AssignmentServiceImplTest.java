package co.unimagdalena.services;

import co.unimagdalena.domine.entities.User;
import co.unimagdalena.domine.entities.UserRole;
import co.unimagdalena.domine.entities.UserStatus;
import co.unimagdalena.domine.repositories.AssignmentRepository;
import co.unimagdalena.domine.repositories.UserRepository;
import co.unimagdalena.services.impl.AssignmentServiceImpl;
import co.unimagdalena.services.mapper.AssignmentMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
public class AssignmentServiceImplTest {
    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private TripService tripService;

    @Mock
    private UserRepository userRepository;

    @Spy
    private AssignmentMapper assignmentMapper = Mappers.getMapper(AssignmentMapper.class);

    @InjectMocks
    private AssignmentServiceImpl assignmentService;

    private User createUser(Long id, String email, String name,
                            String phone, UserRole role, UserStatus status,
                            String password, LocalDateTime createdAt) {
        return User.builder().
                id(id).email(email).
                fullName(name).phone(phone).
                role(role).status(status).
                passwordHash(password).createdAt(createdAt).build();
    }

    //1
    @Test
    @DisplayName("Debe asignar conductor a un viaje de manera exitosa")
    void shouldAssignDriverToTrip(){}

    @Test
    @DisplayName("Debe lanzar excepcion si el Usuario asignado no es un conductor")
    void shouldThrowExceptionWhenAssignedDriverIsNotDriver(){}

    @Test
    @DisplayName("Debe lanza excepcion si el conductor que se desea " +
            "asignar tiene conflictos con los viajes a los que ya pertenece")
    void shouldThrowExceptionWhenDriverHasConflictingTrip(){}

    @Test
    @DisplayName("Debe actualizar el Conductor de la asignacion " +
            "cuando el viaje ya tienen uno asignado")
    void shouldUpdateAssignmentDriverWhenTripAlreadyHasOne(){}

    @Test
    @DisplayName("Debe lanzar excepcion cuando el Conductor que se dese asignar no existe")
    void shouldThrowExceptionWhenDriverDoesNotExist(){}

    @Test
    @DisplayName("Debe lanzar excepcion cuando el Viaje no existe")
    void shouldThrowExceptionWhenTripDoesNotExist(){}

    //2
    @Test
    @DisplayName("Debe asignar de manera exitosa el Bus al Viaje")
    void shouldAssignBusToTrip(){}

    @Test
    @DisplayName("Debe lanzar excepcion cuando el Bus que se desea " +
            "asignar tiene conflicto con sus Viajes ya asignados")
    void shouldThrowExceptionWhenBusHasConflictingTrips(){}

    //3
    @Test
    @DisplayName("Debe actualizar de manera existosa la Asignacion")
    void shoudlUpdateAssignment(){}

    @Test
    @DisplayName("Debe lanzar excepcion cuando no se encuentra la Asignacion")
    void shouldThrowExceptionWhenAssignmentNotFound(){}

    //4
    @Test
    @DisplayName("Debe eliminar de manera exitosa la Asignacion")
    void shouldDeleteAssignment(){}

    //5
    @Test
    @DisplayName("Debe obtener de manera exitosa la Asignacion")
    void shouldGetAssignment(){}

    //6
    @Test
    @DisplayName("Debe obtener la Asignacion dado el identificador del Viaje")
    void shouldGetAssignmentByTripId(){}

    @Test
    @DisplayName("Debe lanzar excepcion cuando no se encontro " +
            "la Asignacion dado el identificador del Viaje")
    void shouldThrowExceptionWhenAssignmentNotFoundByTrip(){}

    //7
    @Test
    @DisplayName("Debe obtener la Asignacion dado el identificador del Conductor")
    void shouldGetAssignmentsByDriverId(){}


}