package it.uniroma3.siw.travelly.controller;

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
            Model model) {


        /*
         * Mostriamo nella homepage
         * gli itinerari ufficiali Travelly.
         */
        model.addAttribute(
                "templates",
                tripService.findTemplates()
        );


        return "index";
    }
}