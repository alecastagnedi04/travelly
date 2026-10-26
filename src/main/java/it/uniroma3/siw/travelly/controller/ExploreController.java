package it.uniroma3.siw.travelly.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import it.uniroma3.siw.travelly.service.TripService;


@Controller
public class ExploreController {


    private final TripService tripService;



    public ExploreController(
            TripService tripService) {

        this.tripService = tripService;
    }



    /*
     * =====================================================
     * EXPLORE + RICERCA
     * =====================================================
     *
     * Esempio:
     *
     * GET /explore?q=Copenhagen
     *
     * Se q è vuoto mostriamo tutti gli itinerari
     * e tutti i viaggi pubblici.
     *
     * Se q contiene testo effettuiamo
     * una ricerca esatta case-insensitive.
     */
    @GetMapping("/explore")
    public String explore(

            @RequestParam(
                    value = "q",
                    required = false,
                    defaultValue = ""
            )
            String q,

            Model model) {


        /*
         * Rimuoviamo eventuali spazi
         * all'inizio e alla fine.
         */
        String searchQuery = q.trim();


        /*
         * Controlliamo se l'utente
         * ha effettivamente cercato qualcosa.
         */
        boolean searchPerformed =
                !searchQuery.isBlank();



        /*
         * =================================================
         * CASO 1: RICERCA ATTIVA
         * =================================================
         */
        if (searchPerformed) {


            /*
             * Cerchiamo negli itinerari ufficiali.
             */
            model.addAttribute(
                    "templates",
                    tripService
                            .findTemplatesByExactName(
                                    searchQuery
                            )
            );


            /*
             * Cerchiamo nei viaggi pubblici.
             *
             * I viaggi PRIVATE non vengono
             * restituiti dal repository.
             */
            model.addAttribute(
                    "communityTrips",
                    tripService
                            .findPublicCommunityTripsByExactName(
                                    searchQuery
                            )
            );
        }



        /*
         * =================================================
         * CASO 2: NESSUNA RICERCA
         * =================================================
         */
        else {


            model.addAttribute(
                    "templates",
                    tripService.findTemplates()
            );


            model.addAttribute(
                    "communityTrips",
                    tripService.findPublicCommunityTrips()
            );
        }



        /*
         * Manteniamo il testo cercato
         * nel campo della barra di ricerca.
         */
        model.addAttribute(
                "searchQuery",
                searchQuery
        );



        /*
         * Serve al template Thymeleaf
         * per mostrare un messaggio
         * quando non vengono trovati risultati.
         */
        model.addAttribute(
                "searchPerformed",
                searchPerformed
        );



        /*
         * loggedIn, username e isAdmin
         * sono già disponibili grazie a
         * GlobalModelAttributes.
         */
        return "explore/list";
    }



    /*
     * =====================================================
     * DETTAGLIO TEMPLATE UFFICIALE
     * =====================================================
     */
    @GetMapping("/explore/{id}")
    public String showTemplate(

            @PathVariable("id")
            Long id,

            Model model) {


        return tripService
                .findTemplateById(id)
                .map(trip -> {


                    model.addAttribute(
                            "trip",
                            trip
                    );


                    return "explore/detail";
                })
                .orElse(
                        "redirect:/explore"
                );
    }



    /*
     * =====================================================
     * DETTAGLIO VIAGGIO PUBBLICO
     * =====================================================
     */
    @GetMapping("/explore/community/{id}")
    public String showCommunityTrip(

            @PathVariable("id")
            Long id,

            Model model) {


        return tripService
                .findPublicCommunityTripById(id)
                .map(trip -> {


                    model.addAttribute(
                            "trip",
                            trip
                    );


                    return "explore/community-detail";
                })
                .orElse(
                        "redirect:/explore"
                );
    }
}