package it.uniroma3.siw.travelly.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;


@ControllerAdvice
public class GlobalModelAttributes {


    /*
     * =====================================================
     * ATTRIBUTI GLOBALI DEL MODEL
     * =====================================================
     *
     * Questo metodo viene eseguito prima dei controller MVC.
     *
     * In questo modo tutte le pagine Thymeleaf
     * possono utilizzare:
     *
     * - loggedIn
     * - username
     * - isAdmin
     *
     * senza doverli aggiungere manualmente
     * in ogni singolo controller.
     */
    @ModelAttribute
    public void addGlobalAttributes(
            Model model,
            Authentication authentication) {


        /*
         * Verifichiamo se esiste realmente
         * un utente autenticato.
         */
        boolean loggedIn =
                authentication != null
                &&
                authentication.isAuthenticated()
                &&
                !(authentication
                        instanceof AnonymousAuthenticationToken);


        model.addAttribute(
                "loggedIn",
                loggedIn
        );



        /*
         * Utente non autenticato.
         */
        if (!loggedIn) {


            model.addAttribute(
                    "isAdmin",
                    false
            );


            return;
        }



        /*
         * =================================================
         * USERNAME
         * =================================================
         *
         * Da ora è disponibile in tutti i template.
         */
        model.addAttribute(
                "username",
                authentication.getName()
        );



        /*
         * =================================================
         * ADMIN
         * =================================================
         */
        boolean isAdmin =
                authentication
                        .getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority
                                        .getAuthority()
                                        .equals(
                                                "ROLE_ADMIN"
                                        )
                        );


        model.addAttribute(
                "isAdmin",
                isAdmin
        );
    }
}