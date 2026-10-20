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
     * Form creazione Destination.
     */
    @GetMapping("/trips/{tripId}/destinations/new")
    public String showCreateForm(
            @PathVariable("tripId")
            Long tripId,

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
     * Creazione Destination.
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


        return "redirect:/trips/" + tripId;
    }


    /*
     * Mostra la form di modifica.
     */
    @GetMapping(
        "/trips/{tripId}/destinations/{destinationId}/edit"
    )
    public String showEditForm(
            @PathVariable("tripId")
            Long tripId,

            @PathVariable("destinationId")
            Long destinationId,

            Model model,

            Principal principal) {


        return destinationService
                .findByIdAndOwnerUsername(
                        tripId,
                        destinationId,
                        principal.getName()
                )
                .map(destination -> {

                    model.addAttribute(
                            "destination",
                            destination
                    );

                    model.addAttribute(
                            "trip",
                            destination.getTrip()
                    );

                    return "destinations/edit";
                })
                .orElse("redirect:/trips");
    }


    /*
     * Salva le modifiche.
     */
    @PostMapping(
        "/trips/{tripId}/destinations/{destinationId}/edit"
    )
    public String updateDestination(
            @PathVariable("tripId")
            Long tripId,

            @PathVariable("destinationId")
            Long destinationId,

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


        destinationService.updateDestination(
                tripId,
                destinationId,
                city,
                country,
                arrivalDate,
                departureDate,
                principal.getName()
        );


        return "redirect:/trips/" + tripId;
    }


    /*
     * Eliminazione Destination.
     */
    @PostMapping(
        "/trips/{tripId}/destinations/{destinationId}/delete"
    )
    public String deleteDestination(
            @PathVariable("tripId")
            Long tripId,

            @PathVariable("destinationId")
            Long destinationId,

            Principal principal) {


        destinationService.deleteDestination(
                tripId,
                destinationId,
                principal.getName()
        );


        return "redirect:/trips/" + tripId;
    }
}