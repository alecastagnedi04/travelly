package it.uniroma3.siw.travelly.controller;

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

    public DestinationController(DestinationService destinationService,
                                 TripService tripService) {

        this.destinationService = destinationService;
        this.tripService = tripService;
    }

    @GetMapping("/trips/{tripId}/destinations/new")   //get (mostrare la form)
    public String showCreateForm(
            @PathVariable("tripId") Long tripId,
            Model model) {

        return tripService.findById(tripId)
                .map(trip -> {
                    model.addAttribute("trip", trip);
                    return "destinations/form";
                })
                .orElse("redirect:/trips");
    }
 
    @PostMapping("/trips/{tripId}/destinations")   //post (salvare la destinazione)
    public String createDestination(
            @PathVariable("tripId") Long tripId,
            @RequestParam("city") String city,
            @RequestParam("country") String country,
            @RequestParam("arrivalDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate arrivalDate,
            @RequestParam("departureDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate departureDate) {

        if (destinationService.createDestination(
                tripId,
                city,
                country,
                arrivalDate,
                departureDate
        ).isPresent()) {

            return "redirect:/trips/" + tripId;
        }

        return "redirect:/trips";
    }
}