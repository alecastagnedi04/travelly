package it.uniroma3.siw.travelly.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.travelly.model.Role;
import it.uniroma3.siw.travelly.model.User;
import it.uniroma3.siw.travelly.repository.UserRepository;


@Service
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;


    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository =
                userRepository;

        this.passwordEncoder =
                passwordEncoder;
    }


    /*
     * Controlla se esiste già
     * un determinato username.
     */
    @Transactional(readOnly = true)
    public boolean usernameExists(
            String username) {

        return userRepository
                .existsByUsername(username);
    }


    /*
     * Controlla se esiste già
     * una determinata email.
     */
    @Transactional(readOnly = true)
    public boolean emailExists(
            String email) {

        return userRepository
                .existsByEmail(email);
    }


    /*
     * Registra un nuovo utente.
     *
     * La password viene cifrata prima
     * del salvataggio.
     *
     * Un utente registrato dal sito
     * riceve sempre il ruolo USER.
     */
    @Transactional
    public User registerUser(
            String username,
            String email,
            String rawPassword) {

        User user =
                new User();


        user.setUsername(
                username
        );

        user.setEmail(
                email
        );


        user.setPassword(
                passwordEncoder.encode(
                        rawPassword
                )
        );


        user.setRole(
                Role.USER
        );


        return userRepository.save(user);
    }


    /*
     * Utilizzato dall'amministratore
     * per visualizzare tutti gli utenti.
     */
    @Transactional(readOnly = true)
    public Iterable<User> findAll() {

        return userRepository.findAll();
    }
}