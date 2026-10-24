package it.uniroma3.siw.travelly.controller;

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
     * ELENCO PUBBLICO ITINERARI
     * =====================================================
     */
    @GetMapping("/explore")
    public String explore(
            Model model) {


        model.addAttribute(
                "templates",
                tripService.findTemplates()
        );


        return "explore/list";
    }



    /*
     * =====================================================
     * DETTAGLIO PUBBLICO ITINERARIO
     * =====================================================
     */
    @GetMapping("/explore/{id}")
    public String showTemplate(

            @PathVariable("id")
            Long id,

            Model model) {


        return tripService
                .findTemplateById(
                        id
                )
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
}