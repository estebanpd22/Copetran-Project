package co.unimagdalena.services;

import co.unimagdalena.api.dto.PassengerDto.*;
import co.unimagdalena.domine.entities.Passenger;
import co.unimagdalena.domine.entities.User;
import co.unimagdalena.domine.entities.UserRole;
import co.unimagdalena.domine.entities.UserStatus;
import co.unimagdalena.domine.repositories.PassengerRepository;
import co.unimagdalena.domine.repositories.UserRepository;
import co.unimagdalena.exception.NotFoundException;
import co.unimagdalena.services.impl.PassengerServiceImpl;
import co.unimagdalena.services.mapper.PassengerMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PassengerServiceImplTest {

    @Mock
    private PassengerRepository passengerRepository;

    @Mock
    private PassengerMapper passengerMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PassengerServiceImpl passengerService;

    private PassengerCreateRequest createRequest;
    private PassengerUpdateRequest updateRequest;
    private Passenger passenger;
    private PassengerResponse passengerResponse;
    private User user;

    @BeforeEach
    void setUp() {
        createRequest = new PassengerCreateRequest(
                "Juan Perez",
                "CC",
                "1234567890",
                LocalDate.of(1990, 5, 15),
                "+573001234567",
                1L
        );

        updateRequest = new PassengerUpdateRequest(
                2L,
                "Juan Perez Updated",
                "CC",
                "1234567890",
                LocalDate.of(1990, 5, 15),
                "+573009876543"
        );

        user = User.builder()
                .id(1L)
                .fullName("Juan Perez")
                .email("juan@example.com")
                .phone("+573001234567")
                .role(UserRole.PASSENGER)
                .status(UserStatus.ACTIVE)
                .build();

        passenger = Passenger.builder()
                .id(1L)
                .fullName("Juan Perez")
                .documentType("CC")
                .documentNumber("1234567890")
                .birthDate(LocalDate.of(1990, 5, 15))
                .phoneNumber("+573001234567")
                .createdAt(OffsetDateTime.now())
                .user(user)
                .build();

        passengerResponse = new PassengerResponse(
                1L,
                "Juan Perez",
                "CC",
                "1234567890",
                LocalDate.of(1990, 5, 15),
                "+573001234567"
        );
    }

    // ==================== CREATE PASSENGER ====================

    @Test
    @DisplayName("Should create passenger successfully with user")
    void shouldCreatePassengerSuccessfullyWithUser() {
        // Arrange
        when(passengerRepository.existsByDocumentNumber(createRequest.documentNumber())).thenReturn(false);
        when(passengerMapper.toEntity(createRequest)).thenReturn(passenger);
        when(userRepository.findById(createRequest.userId())).thenReturn(Optional.of(user));
        when(passengerRepository.save(any(Passenger.class))).thenReturn(passenger);
        when(passengerMapper.toResponse(passenger)).thenReturn(passengerResponse);

        // Act
        PassengerResponse result = passengerService.createPassenger(createRequest);

        // Assert
        assertNotNull(result);
        assertEquals(passengerResponse.id(), result.id());
        verify(passengerRepository).existsByDocumentNumber(createRequest.documentNumber());
        verify(userRepository).findById(createRequest.userId());
        verify(passengerRepository).save(any(Passenger.class));
        verify(passengerMapper).toResponse(passenger);
    }

    @Test
    @DisplayName("Should create passenger successfully as guest without user")
    void shouldCreatePassengerAsGuestWithoutUser() {
        // Arrange
        PassengerCreateRequest guestRequest = new PassengerCreateRequest(
                "Guest Passenger",
                "CC",
                "9876543210",
                LocalDate.of(1995, 3, 20),
                "+573009999999",
                null // Sin userId - guest
        );
        Passenger guestPassenger = Passenger.builder()
                .id(2L)
                .fullName("Guest Passenger")
                .documentNumber("9876543210")
                .user(null)
                .build();

        when(passengerRepository.existsByDocumentNumber(guestRequest.documentNumber())).thenReturn(false);
        when(passengerMapper.toEntity(guestRequest)).thenReturn(guestPassenger);
        when(passengerRepository.save(any(Passenger.class))).thenReturn(guestPassenger);
        when(passengerMapper.toResponse(guestPassenger)).thenReturn(passengerResponse);

        // Act
        PassengerResponse result = passengerService.createPassenger(guestRequest);

        // Assert
        assertNotNull(result);
        verify(userRepository, never()).findById(any());
        verify(passengerRepository).save(any(Passenger.class));
    }

    @Test
    @DisplayName("Should throw IllegalStateException when document number is duplicated")
    void shouldThrowExceptionWhenDocumentNumberIsDuplicated() {
        // Arrange
        when(passengerRepository.existsByDocumentNumber(createRequest.documentNumber())).thenReturn(true);

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> passengerService.createPassenger(createRequest)
        );

        assertTrue(exception.getMessage().contains("Ya existe un pasajero con el documento"));
        verify(passengerRepository).existsByDocumentNumber(createRequest.documentNumber());
        verify(passengerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw NotFoundException when user does not exist")
    void shouldThrowExceptionWhenUserDoesNotExist() {
        // Arrange
        when(passengerRepository.existsByDocumentNumber(createRequest.documentNumber())).thenReturn(false);
        when(passengerMapper.toEntity(createRequest)).thenReturn(passenger);
        when(userRepository.findUserById(createRequest.userId())).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> passengerService.createPassenger(createRequest)
        );

        assertTrue(exception.getMessage().contains("Usuario con ID"));
        assertTrue(exception.getMessage().contains("no encontrado"));
        verify(passengerRepository, never()).save(any());
    }

    // ==================== UPDATE PASSENGER ====================

    @Test
    @DisplayName("Should update passenger successfully")
    void shouldUpdatePassengerSuccessfully() {
        // Arrange
        when(passengerRepository.findPassengerById(1L)).thenReturn(Optional.of(passenger));
        doNothing().when(passengerMapper).updateEntityFromRequest(updateRequest, passenger);
        when(passengerRepository.save(passenger)).thenReturn(passenger);

        // Act
        passengerService.updatePassenger(1L, updateRequest);

        // Assert
        verify(passengerRepository).findPassengerById(1L);
        verify(passengerMapper).updateEntityFromRequest(updateRequest, passenger);
        verify(passengerRepository).save(passenger);
    }

    @Test
    @DisplayName("Should throw NotFoundException when passenger to update does not exist")
    void shouldThrowExceptionWhenPassengerToUpdateDoesNotExist() {
        // Arrange
        when(passengerRepository.findPassengerById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> passengerService.updatePassenger(999L, updateRequest)
        );

        assertTrue(exception.getMessage().contains("Pasajero con ID"));
        assertTrue(exception.getMessage().contains("no encontrado"));
        verify(passengerRepository).findPassengerById(999L);
        verify(passengerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should re-associate user when updating passenger with new userId")
    void shouldReAssociateUserWhenUpdatingPassenger() {
        // Arrange
        User newUser = User.builder()
                .id(2L)
                .fullName("New User")
                .email("newuser@example.com")
                .build();

        when(passengerRepository.findPassengerById(1L)).thenReturn(Optional.of(passenger));
        when(userRepository.findById(2L)).thenReturn(Optional.of(newUser));
        doNothing().when(passengerMapper).updateEntityFromRequest(updateRequest, passenger);
        when(passengerRepository.save(passenger)).thenReturn(passenger);

        // Act
        passengerService.updatePassenger(1L, updateRequest);

        // Assert
        verify(passengerRepository).findPassengerById(1L);
        verify(userRepository).findUserById(2L);
        verify(passengerMapper).updateEntityFromRequest(updateRequest, passenger);
        verify(passengerRepository).save(passenger);
    }

    @Test
    @DisplayName("Should throw NotFoundException when new user does not exist during update")
    void shouldThrowExceptionWhenNewUserDoesNotExist() {
        // Arrange
        when(passengerRepository.findPassengerById(1L)).thenReturn(Optional.of(passenger));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        doNothing().when(passengerMapper).updateEntityFromRequest(updateRequest, passenger);

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> passengerService.updatePassenger(1L, updateRequest)
        );

        assertTrue(exception.getMessage().contains("Usuario con ID"));
        assertTrue(exception.getMessage().contains("no encontrado"));
        verify(passengerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should not re-associate user when userId is null in update request")
    void shouldNotReAssociateUserWhenUserIdIsNull() {
        // Arrange
        PassengerUpdateRequest requestWithoutUser = new PassengerUpdateRequest(
                null, // userId null
                "Updated Name",
                "CC",
                "1234567890",
                LocalDate.of(1990, 5, 15),
                "+573009876543"
        );

        when(passengerRepository.findPassengerById(1L)).thenReturn(Optional.of(passenger));
        doNothing().when(passengerMapper).updateEntityFromRequest(requestWithoutUser, passenger);
        when(passengerRepository.save(passenger)).thenReturn(passenger);

        // Act
        passengerService.updatePassenger(1L, requestWithoutUser);

        // Assert
        verify(passengerRepository).findPassengerById(1L);
        verify(userRepository, never()).findById(any());
        verify(passengerMapper).updateEntityFromRequest(requestWithoutUser, passenger);
        verify(passengerRepository).save(passenger);
    }

    @Test
    @DisplayName("Should not re-associate user when userId is same as current")
    void shouldNotReAssociateUserWhenUserIdIsSame() {
        // Arrange
        PassengerUpdateRequest sameUserRequest = new PassengerUpdateRequest(
                1L, // mismo userId
                "Updated Name",
                "CC",
                "1234567890",
                LocalDate.of(1990, 5, 15),
                "+573009876543"
        );

        when(passengerRepository.findPassengerById(1L)).thenReturn(Optional.of(passenger));
        doNothing().when(passengerMapper).updateEntityFromRequest(sameUserRequest, passenger);
        when(passengerRepository.save(passenger)).thenReturn(passenger);

        // Act
        passengerService.updatePassenger(1L, sameUserRequest);

        // Assert
        verify(passengerRepository).findPassengerById(1L);
        verify(userRepository, never()).findById(any());
        verify(passengerRepository).save(passenger);
    }

    // ==================== DELETE PASSENGER ====================

    @Test
    @DisplayName("Should delete passenger successfully")
    void shouldDeletePassengerSuccessfully() {
        // Arrange
        when(passengerRepository.existsById(1L)).thenReturn(true);
        doNothing().when(passengerRepository).deleteById(1L);

        // Act
        passengerService.deletePassenger(1L);

        // Assert
        verify(passengerRepository).existsById(1L);
        verify(passengerRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw NotFoundException when passenger to delete does not exist")
    void shouldThrowExceptionWhenPassengerToDeleteDoesNotExist() {
        // Arrange
        when(passengerRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> passengerService.deletePassenger(999L)
        );

        assertTrue(exception.getMessage().contains("Pasajero con ID"));
        assertTrue(exception.getMessage().contains("no encontrado"));
        verify(passengerRepository).existsById(999L);
        verify(passengerRepository, never()).deleteById(any());
    }

    // ==================== GET PASSENGERS BY USER ====================

    @Test
    @DisplayName("Should get passengers by user successfully")
    void shouldGetPassengersByUserSuccessfully() {
        // Arrange
        Passenger passenger2 = Passenger.builder()
                .id(2L)
                .fullName("Second Passenger")
                .documentNumber("9999999999")
                .user(user)
                .build();

        List<Passenger> passengers = Arrays.asList(passenger, passenger2);

        PassengerResponse response2 = new PassengerResponse(
                2L,
                "Second Passenger",
                "CC",
                "9999999999",
                LocalDate.of(1985, 10, 20),
                "+573001111111"
        );

        when(passengerRepository.findByUserId(1L)).thenReturn(passengers);
        when(passengerMapper.toResponse(passenger)).thenReturn(passengerResponse);
        when(passengerMapper.toResponse(passenger2)).thenReturn(response2);

        // Act
        List<PassengerResponse> result = passengerService.getPassengerByUser(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(passengerRepository).findByUserId(1L);
        verify(passengerMapper, times(2)).toResponse(any(Passenger.class));
    }

    @Test
    @DisplayName("Should return empty list when user has no passengers")
    void shouldReturnEmptyListWhenUserHasNoPassengers() {
        // Arrange
        when(passengerRepository.findByUserId(999L)).thenReturn(Collections.emptyList());

        // Act
        List<PassengerResponse> result = passengerService.getPassengerByUser(999L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(passengerRepository).findByUserId(999L);
        verify(passengerMapper, never()).toResponse(any());
    }

    // ==================== FIND BY DOCUMENT NUMBER ====================

    @Test
    @DisplayName("Should find passenger by document number successfully")
    void shouldFindPassengerByDocumentNumberSuccessfully() {
        // Arrange
        when(passengerRepository.findByDocumentNumber("1234567890")).thenReturn(Optional.of(passenger));
        when(passengerMapper.toResponse(passenger)).thenReturn(passengerResponse);

        // Act
        PassengerResponse result = passengerService.finByDocumentNumber("1234567890");

        // Assert
        assertNotNull(result);
        assertEquals("1234567890", result.documentNumber());
        verify(passengerRepository).findByDocumentNumber("1234567890");
        verify(passengerMapper).toResponse(passenger);
    }

    @Test
    @DisplayName("Should throw NotFoundException when passenger with document number does not exist")
    void shouldThrowExceptionWhenPassengerWithDocumentNotFound() {
        // Arrange
        when(passengerRepository.findByDocumentNumber("9999999999")).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> passengerService.finByDocumentNumber("9999999999")
        );

        assertTrue(exception.getMessage().contains("Pasajero no encontrado con el documento"));
        verify(passengerRepository).findByDocumentNumber("9999999999");
        verify(passengerMapper, never()).toResponse(any());
    }

    // ==================== GET PASSENGER BY ID ====================

    @Test
    @DisplayName("Should get passenger by ID successfully")
    void shouldGetPassengerByIdSuccessfully() {
        // Arrange
        when(passengerRepository.findPassengerById(1L)).thenReturn(Optional.of(passenger));
        when(passengerMapper.toResponse(passenger)).thenReturn(passengerResponse);

        // Act
        PassengerResponse result = passengerService.getPassengerById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        verify(passengerRepository).findPassengerById(1L);
        verify(passengerMapper).toResponse(passenger);
    }

    @Test
    @DisplayName("Should throw NotFoundException when passenger by ID does not exist")
    void shouldThrowExceptionWhenPassengerByIdNotFound() {
        // Arrange
        when(passengerRepository.findPassengerById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> passengerService.getPassengerById(999L)
        );

        assertTrue(exception.getMessage().contains("Pasajero con ID"));
        assertTrue(exception.getMessage().contains("no encontrado"));
        verify(passengerRepository).findPassengerById(999L);
        verify(passengerMapper, never()).toResponse(any());
    }

    // ==================== HELPER METHODS ====================

    private Passenger createPassenger(Long id, String documentNumber, String fullName) {
        return Passenger.builder()
                .id(id)
                .fullName(fullName)
                .documentType("CC")
                .documentNumber(documentNumber)
                .birthDate(LocalDate.of(1990, 1, 1))
                .phoneNumber("+573001234567")
                .createdAt(OffsetDateTime.now())
                .build();
    }

    private User createUser(Long id, String fullName, String email) {
        return User.builder()
                .id(id)
                .fullName(fullName)
                .email(email)
                .phone("+573001234567")
                .role(UserRole.PASSENGER)
                .status(UserStatus.ACTIVE)
                .build();
    }
}
