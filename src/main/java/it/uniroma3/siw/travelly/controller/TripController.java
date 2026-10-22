package it.uniroma3.siw.travelly.controller;

import java.security.Principal;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import it.uniroma3.siw.travelly.service.TripService;

@Controller
public class TripController {

    private final TripService tripService;


    public TripController(TripService tripService) {
        this.tripService = tripService;
    }


    /*
     * Mostra solamente i viaggi appartenenti
     * all'utente autenticato.
     */
    @GetMapping("/trips")
    public String showTrips(
            Model model,
            Authentication authentication) {

        String username = authentication.getName();

        model.addAttribute(
                "trips",
                tripService.findByOwnerUsername(username)
        );

        model.addAttribute(
                "username",
                username
        );

        boolean isAdmin = authentication
                .getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN")
                );

        model.addAttribute(
                "isAdmin",
                isAdmin
        );

        return "trips/list";
    }


    /*
     * Mostra la form per creare un nuovo viaggio.
     */
    @GetMapping("/trips/new")
public String showCreateForm(
        Model model,
        Principal principal) {

    model.addAttribute(
            "username",
            principal.getName()
    );

    return "trips/form";
}


    /*
     * Crea un nuovo viaggio e lo associa
     * all'utente autenticato.
     */
    @PostMapping("/trips")
    public String createTrip(
            @RequestParam("name")
            String name,

            @RequestParam(
                    value = "description",
                    defaultValue = ""
            )
            String description,

            @RequestParam("startDate")
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate startDate,

            @RequestParam("endDate")
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate endDate,

            @RequestParam("budget")
            Double budget,

            Principal principal) {

        tripService.createTrip(
                name,
                description,
                startDate,
                endDate,
                budget,
                principal.getName()
        );

        return "redirect:/trips";
    }


    /*
     * Mostra il dettaglio del viaggio solamente
     * se appartiene all'utente autenticato.
     */
    @GetMapping("/trips/{id}")
    public String showTrip(
            @PathVariable("id")
            Long id,

            Model model,

            Authentication authentication) {

        return tripService
                .findByIdAndOwnerUsername(
                        id,
                        authentication.getName()
                )
                .map(trip -> {

                    model.addAttribute(
                            "trip",
                            trip
                    );

                    model.addAttribute(
                            "totalExpenses",
                            tripService.calculateTotalExpenses(trip)
                    );

                    model.addAttribute(
                            "remainingBudget",
                            tripService.calculateRemainingBudget(trip)
                    );

                    model.addAttribute(
                            "username",
                            authentication.getName()
                    );

                    boolean isAdmin = authentication
                            .getAuthorities()
                            .stream()
                            .anyMatch(authority ->
                                    authority.getAuthority()
                                            .equals("ROLE_ADMIN")
                            );

                    model.addAttribute(
                            "isAdmin",
                            isAdmin
                    );

                    return "trips/show";
                })
                .orElse("redirect:/trips");
    }


    /*
     * Mostra la form di modifica solamente
     * se il Trip appartiene all'utente autenticato.
     */
    @GetMapping("/trips/{id}/edit")
public String showEditForm(
        @PathVariable("id")
        Long id,

        Model model,

        Principal principal) {

    return tripService
            .findByIdAndOwnerUsername(
                    id,
                    principal.getName()
            )
            .map(trip -> {

                model.addAttribute(
                        "trip",
                        trip
                );

                model.addAttribute(
                        "username",
                        principal.getName()
                );

                return "trips/edit";
            })
            .orElse("redirect:/trips");
}

    /*
     * Aggiorna il Trip solamente se appartiene
     * all'utente autenticato.
     */
    @PostMapping("/trips/{id}/edit")
    public String updateTrip(
            @PathVariable("id")
            Long id,

            @RequestParam("name")
            String name,

            @RequestParam(
                    value = "description",
                    defaultValue = ""
            )
            String description,

            @RequestParam("startDate")
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate startDate,

            @RequestParam("endDate")
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate endDate,

            @RequestParam("budget")
            Double budget,

            Principal principal) {

        if (tripService.updateTrip(
                id,
                name,
                description,
                startDate,
                endDate,
                budget,
                principal.getName()
        ).isPresent()) {

            return "redirect:/trips/" + id;
        }

        return "redirect:/trips";
    }


    /*
     * Elimina il viaggio solamente se appartiene
     * all'utente autenticato.
     */
    @PostMapping("/trips/{id}/delete")
    public String deleteTrip(
            @PathVariable("id")
            Long id,

            Principal principal) {

        tripService.deleteByIdForOwner(
                id,
                principal.getName()
        );

        return "redirect:/trips";
    }
}