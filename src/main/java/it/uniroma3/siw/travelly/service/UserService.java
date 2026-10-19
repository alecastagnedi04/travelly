package it.uniroma3.siw.travelly.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import it.uniroma3.siw.travelly.model.Role;
import it.uniroma3.siw.travelly.model.User;
import it.uniroma3.siw.travelly.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;  //serve per parlare con il database
    private final PasswordEncoder passwordEncoder;  //serve per trasformare la password prima di salvarla

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public User registerUser(
            String username,
            String email,
            String rawPassword) {

        User user = new User();

        user.setUsername(username);
        user.setEmail(email);

        user.setPassword(
                passwordEncoder.encode(rawPassword)    //non salviamo mai la password originale.
        );

        user.setRole(Role.USER);  //qualunque persona che si registra tramite il sito diventa sempre user.

        return userRepository.save(user);
    }
}