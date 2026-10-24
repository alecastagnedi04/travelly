package it.uniroma3.siw.travelly.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import it.uniroma3.siw.travelly.service.TripService;


@Controller
public class HomeController {


    private final TripService tripService;


    public HomeController(
            TripService tripService) {

        this.tripService =
                tripService;
    }



    @GetMapping("/")
    public String home(
            Model model,
            Principal principal) {


        /*
         * Guide ufficiali Travelly.
         */
        model.addAttribute(
                "templates",
                tripService.findTemplates()
        );


        /*
         * Viaggi pubblici della community.
         */
        model.addAttribute(
                "communityTrips",
                tripService.findPublicCommunityTrips()
        );


        /*
         * Informazioni sull'utente autenticato.
         */
        boolean loggedIn =
                principal != null;


        model.addAttribute(
                "loggedIn",
                loggedIn
        );


        if (loggedIn) {

            model.addAttribute(
                    "username",
                    principal.getName()
            );
        }


        return "index";
    }
}