package co.unimagdalena.services;

import co.unimagdalena.api.dto.UserDto.*;
import co.unimagdalena.domine.entities.UserRole;

import java.util.List;

public interface UserService {

    UserResponse registerUser(UserCreateRequest request);
    UserResponse createEmployee(EmployeeCreateRequest request);

    UserResponse login(String email, String password);
    void changePassword(Long id, String oldPassword, String newPassword);
    void desactivateUser(Long id);
    void reactivateUser(Long id);

    void updateUser(Long id, UserUpdateRequest request);
    UserResponse getUserById(Long id);
    UserResponse getUserByEmail(String email);
    UserResponse getUserByPhone(String phone);
    List<UserResponse> getAllUsersByRole(UserRole role);
}
