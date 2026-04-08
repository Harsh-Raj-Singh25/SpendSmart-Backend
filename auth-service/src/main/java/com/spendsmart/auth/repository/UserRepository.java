package com.spendsmart.auth.repository;

import com.spendsmart.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

// JpaRepository<User, Integer> gives us free CRUD methods (save, findById, findAll, delete etc.)
// The second generic type Integer is the type of the primary key (userId)
// Spring Data JPA auto-generates SQL from method names — no SQL needed
public interface UserRepository extends JpaRepository<User, Integer> {

    // Generates: SELECT * FROM users WHERE email = ?
    // Returns Optional to force the caller to handle the "not found" case safely
    Optional<User> findByEmail(String email);

    // Generates: SELECT COUNT(*) > 0 FROM users WHERE email = ?
    // Used during registration to prevent duplicate emails
    boolean existsByEmail(String email);

    // Generates: SELECT * FROM users WHERE is_active = ?
    // Used by Admin to list active or deactivated users
    List<User> findByIsActive(boolean isActive);

    // Generates: SELECT * FROM users WHERE currency = ?
    // Could be used by Analytics to group users by currency
    List<User> findByCurrency(String currency);

    // Generates: SELECT COUNT(*) FROM users WHERE is_active = ?
    // Useful for Admin dashboard KPIs
    long countByIsActive(boolean isActive);

    // Generates: DELETE FROM users WHERE user_id = ?
    // Used for permanent hard delete by Admin only
    void deleteByUserId(int userId);
}