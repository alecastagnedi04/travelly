package it.uniroma3.siw.travelly.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;

import it.uniroma3.siw.travelly.service.TripService;

@Controller
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @GetMapping("/trips")
    public String showTrips(Model model) {
        model.addAttribute("trips", tripService.findAll());
        return "trips/list";
    }

    @GetMapping("/trips/new")
    public String showCreateForm() {
        return "trips/form";
    }

    @PostMapping("/trips")
    public String createTrip(
            @RequestParam("name") String name,
            @RequestParam(value = "description", defaultValue = "") String description,
            @RequestParam("startDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam("budget") Double budget) {

        tripService.createTrip(
                name,
                description,
                startDate,
                endDate,
                budget
        );

        return "redirect:/trips";
    }

    @GetMapping("/trips/{id}")
    public String showTrip(@PathVariable("id") Long id, Model model) {

    return tripService.findById(id)
            .map(trip -> {
                model.addAttribute("trip", trip);
                return "trips/show";
            })
            .orElse("redirect:/trips");
}

    @GetMapping("/trips/{id}/edit")  //questo metodo non modifica nulla, serve solo a mostrare la form.
    public String showEditForm(@PathVariable("id") Long id, Model model) {

    return tripService.findById(id)
            .map(trip -> {
                model.addAttribute("trip", trip);
                return "trips/edit";
            })
            .orElse("redirect:/trips");
}

    @PostMapping("/trips/{id}/edit")
    public String updateTrip(
        @PathVariable("id") Long id,
        @RequestParam("name") String name,
        @RequestParam(value = "description", defaultValue = "") String description,
        @RequestParam("startDate")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam("endDate")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @RequestParam("budget") Double budget) {

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
}