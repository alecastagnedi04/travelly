package it.uniroma3.siw.travelly.controller;

import java.security.Principal;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import it.uniroma3.siw.travelly.service.DestinationService;
import it.uniroma3.siw.travelly.service.TripService;

@Controller
public class DestinationController {

    private final DestinationService destinationService;
    private final TripService tripService;


    public DestinationController(
            DestinationService destinationService,
            TripService tripService) {

        this.destinationService = destinationService;
        this.tripService = tripService;
    }


    /*
     * Mostra la form solamente se il viaggio
     * appartiene all'utente autenticato.
     */
    @GetMapping("/trips/{tripId}/destinations/new")
    public String showCreateForm(
            @PathVariable("tripId") Long tripId,
            Model model,
            Principal principal) {

        return tripService
                .findByIdAndOwnerUsername(
                        tripId,
                        principal.getName()
                )
                .map(trip -> {

                    model.addAttribute(
                            "trip",
                            trip
                    );

                    return "destinations/form";
                })
                .orElse("redirect:/trips");
    }


    /*
     * Salva la destinazione solamente se il viaggio
     * appartiene all'utente autenticato.
     */
    @PostMapping("/trips/{tripId}/destinations")
    public String createDestination(
            @PathVariable("tripId")
            Long tripId,

            @RequestParam("city")
            String city,

            @RequestParam("country")
            String country,

            @RequestParam("arrivalDate")
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate arrivalDate,

            @RequestParam("departureDate")
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate departureDate,

            Principal principal) {

        if (destinationService.createDestination(
                tripId,
                city,
                country,
                arrivalDate,
                departureDate,
                principal.getName()
        ).isPresent()) {

            return "redirect:/trips/" + tripId;
        }

        return "redirect:/trips";
    }
}