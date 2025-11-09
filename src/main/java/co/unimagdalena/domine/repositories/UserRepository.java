package co.unimagdalena.domine.repositories;

import co.unimagdalena.domine.entities.User;
import co.unimagdalena.domine.entities.UserRole;
import co.unimagdalena.domine.entities.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserById(Long id);
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    boolean existsByEmail(String email);
    List<User> findByRoleAndStatus(UserRole role, UserStatus status);

    //El estado del usuario es dinamico, puede cambiar en cualquier momento
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE User u " +
            "SET u.status = :status " +
            "WHERE u.id = :userId")
    void changeUserStatus(@Param("userId") Long userId, @Param("status") UserStatus status);


}