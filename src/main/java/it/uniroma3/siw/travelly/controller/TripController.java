package it.uniroma3.siw.travelly.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
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

    // Mostra tutti i viaggi
    @GetMapping("/trips")
    public String showTrips(Model model) {

        model.addAttribute(
                "trips",
                tripService.findAll()
        );

        return "trips/list";
    }


    // Mostra la form per creare un nuovo viaggio
    @GetMapping("/trips/new")
    public String showCreateForm() {

        return "trips/form";
    }


    // Crea un nuovo viaggio
    @PostMapping("/trips")
    public String createTrip(
            @RequestParam("name") String name,
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
            Double budget) {

        tripService.createTrip(
                name,
                description,
                startDate,
                endDate,
                budget
        );

        return "redirect:/trips";
    }


    // Mostra il dettaglio di un singolo viaggio
    @GetMapping("/trips/{id}")
    public String showTrip(
            @PathVariable("id") Long id,
            Model model) {

        return tripService.findById(id)
                .map(trip -> {

                    // Oggetto Trip da mostrare nella pagina
                    model.addAttribute(
                            "trip",
                            trip
                    );

                    // Somma di tutte le spese del viaggio
                    model.addAttribute(
                            "totalExpenses",
                            tripService.calculateTotalExpenses(trip)
                    );

                    // Budget iniziale - spese totali
                    model.addAttribute(
                            "remainingBudget",
                            tripService.calculateRemainingBudget(trip)
                    );

                    return "trips/show";
                })
                .orElse("redirect:/trips");
    }


    // Mostra la form per modificare un viaggio
    @GetMapping("/trips/{id}/edit")
    public String showEditForm(
            @PathVariable("id") Long id,
            Model model) {

        return tripService.findById(id)
                .map(trip -> {

                    model.addAttribute(
                            "trip",
                            trip
                    );

                    return "trips/edit";
                })
                .orElse("redirect:/trips");
    }


    // Salva le modifiche di un viaggio
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
            Double budget) {

        if (tripService.updateTrip(
                id,
                name,
                description,
                startDate,
                endDate,
                budget
        ).isPresent()) {

            return "redirect:/trips/" + id;
        }

        return "redirect:/trips";
    }


    // Elimina un viaggio
    @PostMapping("/trips/{id}/delete")
    public String deleteTrip(
            @PathVariable("id") Long id) {

        tripService.deleteById(id);

        return "redirect:/trips";
    }
}