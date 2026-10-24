package it.uniroma3.siw.travelly.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import it.uniroma3.siw.travelly.service.TripService;


@Controller
public class ExploreController {


    private final TripService tripService;


    public ExploreController(
            TripService tripService) {

        this.tripService =
                tripService;
    }



    /*
     * =====================================================
     * EXPLORE
     * =====================================================
     */
    @GetMapping("/explore")
    public String explore(
            Model model,
            Principal principal) {


        /*
         * Guide ufficiali.
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


        model.addAttribute(
                "loggedIn",
                principal != null
        );


        return "explore/list";
    }



    /*
     * =====================================================
     * DETTAGLIO TRAVELLY GUIDE
     * =====================================================
     */
    @GetMapping("/explore/{id}")
    public String showTemplate(

            @PathVariable("id")
            Long id,

            Model model,

            Principal principal) {


        return tripService
                .findTemplateById(
                        id
                )
                .map(trip -> {


                    model.addAttribute(
                            "trip",
                            trip
                    );


                    model.addAttribute(
                            "loggedIn",
                            principal != null
                    );


                    return "explore/detail";
                })
                .orElse(
                        "redirect:/explore"
                );
    }



    /*
     * =====================================================
     * DETTAGLIO VIAGGIO COMMUNITY
     * =====================================================
     */
    @GetMapping("/explore/community/{id}")
    public String showCommunityTrip(

            @PathVariable("id")
            Long id,

            Model model,

            Principal principal) {


        return tripService
                .findPublicCommunityTripById(
                        id
                )
                .map(trip -> {


                    model.addAttribute(
                            "trip",
                            trip
                    );


                    model.addAttribute(
                            "loggedIn",
                            principal != null
                    );


                    return "explore/community-detail";
                })
                .orElse(
                        "redirect:/explore"
                );
    }
}