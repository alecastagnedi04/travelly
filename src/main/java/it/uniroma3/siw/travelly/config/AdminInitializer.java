package it.uniroma3.siw.travelly.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import it.uniroma3.siw.travelly.model.Role;
import it.uniroma3.siw.travelly.model.User;
import it.uniroma3.siw.travelly.repository.UserRepository;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_USERNAME:}")
    private String adminUsername;

    @Value("${ADMIN_EMAIL:}")
    private String adminEmail;

    @Value("${ADMIN_PASSWORD:}")
    private String adminPassword;

    public AdminInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        if (adminUsername.isBlank()
                || adminEmail.isBlank()
                || adminPassword.isBlank()) {

            return;
        }

        if (userRepository
                .findByUsername(adminUsername)
                .isPresent()) {

            return;
        }

        User admin = new User();

        admin.setUsername(adminUsername);
        admin.setEmail(adminEmail);

        admin.setPassword(
                passwordEncoder.encode(adminPassword)
        );

        admin.setRole(Role.ADMIN);

        userRepository.save(admin);
    }
}