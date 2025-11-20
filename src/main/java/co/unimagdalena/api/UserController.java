package co.unimagdalena.api;

import co.unimagdalena.api.dto.UserDto.*;
import co.unimagdalena.domine.entities.UserRole;
import co.unimagdalena.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/employees")
    public ResponseEntity<UserResponse> createEmployee(
            @Valid @RequestBody EmployeeCreateRequest request,
            UriComponentsBuilder uriBuilder) {
        var employeeCreated = userService.createEmployee(request);
        var location = uriBuilder.path("/api/v1/users/{id}")
                .buildAndExpand(employeeCreated.id())
                .toUri();

        return ResponseEntity.created(location).body(employeeCreated);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> update(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {

        userService.updateUser(id, request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/change-password")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long id,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {

        userService.changePassword(id, oldPassword, newPassword);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        userService.desactivateUser(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<Void> reactivate(@PathVariable Long id) {
        userService.reactivateUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/by-email/{email}")
    public ResponseEntity<UserResponse> getByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @GetMapping("/by-phone/{phone}")
    public ResponseEntity<UserResponse> getByPhone(@PathVariable String phone) {
        return ResponseEntity.ok(userService.getUserByPhone(phone));
    }

    @GetMapping("/by-role/{role}")
    public ResponseEntity<List<UserResponse>> getByRole(@PathVariable UserRole role) {
        return ResponseEntity.ok(userService.getAllUsersByRole(role));
    }
}
