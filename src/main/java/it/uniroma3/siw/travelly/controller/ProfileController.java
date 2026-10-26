package it.uniroma3.siw.travelly.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class ProfileController {


    /*
     * =====================================================
     * PROFILO UTENTE
     * =====================================================
     *
     * username, loggedIn e isAdmin
     * vengono forniti globalmente da:
     *
     * GlobalModelAttributes
     *
     * Non è quindi necessario recuperare
     * nuovamente i viaggi dell'utente.
     */
    @GetMapping("/profile")
    public String profile() {


        return "profile";
    }
}