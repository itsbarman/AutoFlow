package com.autoflow.security;

import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates an initial admin user on startup if none exists, so the system can be
 * logged into. The password comes from an environment variable; using the
 * built-in default logs a clear warning.
 */
@Component
public class AdminUserInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminUserInitializer.class);
    private static final String DEFAULT_PASSWORD = "admin123";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminPassword;

    public AdminUserInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${autoflow.security.admin.username:admin}") String adminUsername,
            @Value("${autoflow.security.admin.password:" + DEFAULT_PASSWORD + "}") String adminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByUsernameIgnoreCase(adminUsername)) {
            return;
        }

        User admin = new User();
        admin.setUsername(adminUsername);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setFullName("Administrator");
        admin.setEnabled(true);
        admin.setRoles(Set.of(Role.ADMIN));
        userRepository.save(admin);

        log.info("Created initial admin user '{}'", adminUsername);
        if (DEFAULT_PASSWORD.equals(adminPassword)) {
            log.warn("Admin user is using the DEFAULT development password. "
                    + "Set autoflow.security.admin.password (env AUTOFLOW_SECURITY_ADMIN_PASSWORD) in production.");
        }
    }
}
