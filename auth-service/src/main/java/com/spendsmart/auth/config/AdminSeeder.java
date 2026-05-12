package com.spendsmart.auth.config;

import com.spendsmart.auth.entity.User;
import com.spendsmart.auth.model.enums.Role;
import com.spendsmart.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// ============================================================================
// ADMIN SEEDER — Auto-creates an ADMIN user on first startup.
//
// HOW IT WORKS:
// - CommandLineRunner runs ONCE after Spring Boot finishes loading all beans
// - It checks if an admin account already exists (by email)
// - If not, it creates one with:
//   Email:    admin@spendsmart.com
//   Password: Admin@123
//   Role:     ADMIN
//
// WHEN TO USE:
// - On fresh database setups (first run after creating spendsmart_auth DB)
// - In development/testing environments
//
// SECURITY NOTE:
// - Change the admin password immediately after first login in production!
// - You can delete this class entirely after you've set up your admin account
// ============================================================================
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder implements CommandLineRunner {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	// This method runs automatically after Spring Boot starts up
	@Override
	public void run(String... args) {
		String adminEmail = "admin@spendsmart1.com";

		// Only create if the admin doesn't already exist
		if (!userRepository.existsByEmail(adminEmail)) {
			User admin = User.builder()
					.fullName("SpendSmart Admin")
					.email(adminEmail)
					.passwordHash(passwordEncoder.encode("pass@078"))
					.role(Role.ADMIN)
					.build();

			userRepository.save(admin);
			log.info("═══════════════════════════════════════════════════");
			log.info("  ADMIN ACCOUNT CREATED SUCCESSFULLY!");
			log.info("  Email:    admin@spendsmart.com");
			log.info("  Password: pass@078");
			log.info("  ⚠️  CHANGE THIS PASSWORD AFTER FIRST LOGIN!");
			log.info("═══════════════════════════════════════════════════");
		} else {
			log.info("Admin account already exists. Skipping seed.");
		}
	}
}
