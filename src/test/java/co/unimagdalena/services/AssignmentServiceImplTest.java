package co.unimagdalena.services;

import co.unimagdalena.domine.entities.User;
import co.unimagdalena.domine.entities.UserRole;
import co.unimagdalena.domine.entities.UserStatus;
import co.unimagdalena.domine.repositories.AssignmentRepository;
import co.unimagdalena.domine.repositories.UserRepository;
import co.unimagdalena.services.impl.AssignmentServiceImpl;
import co.unimagdalena.services.mapper.AssignmentMapper;
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
}
