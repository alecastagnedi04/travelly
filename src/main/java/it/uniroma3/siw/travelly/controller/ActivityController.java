package it.uniroma3.siw.travelly.controller;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import it.uniroma3.siw.travelly.model.ActivityCategory;
import it.uniroma3.siw.travelly.service.ActivityService;
import it.uniroma3.siw.travelly.service.TripService;

@Controller
public class ActivityController {

    private final ActivityService activityService;
    private final TripService tripService;

    public ActivityController(ActivityService activityService,
                              TripService tripService) {

        this.activityService = activityService;
        this.tripService = tripService;
    }

    @GetMapping("/trips/{tripId}/activities/new")
    public String showCreateForm(
            @PathVariable("tripId") Long tripId,
            Model model) {

        return tripService.findById(tripId)
                .map(trip -> {

                    model.addAttribute("trip", trip);
                    model.addAttribute(
                            "categories",
                            ActivityCategory.values()  //restituisce tutti i valori dell'enum ActivityCategory.
                    );

                    return "activities/form";
                })
                .orElse("redirect:/trips");
    }

    @PostMapping("/trips/{tripId}/activities")
    public String createActivity(
            @PathVariable("tripId") Long tripId,
            @RequestParam("name") String name,
            @RequestParam(value = "description", defaultValue = "")
            String description,
            @RequestParam("date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @RequestParam("time")
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            LocalTime time,
            @RequestParam("location") String location,
            @RequestParam("category") ActivityCategory category,
            @RequestParam("cost") Double cost) {

        if (activityService.createActivity(
                tripId,
                name,
                description,
                date,
                time,
                location,
                category,
                cost
        ).isPresent()) {

            return "redirect:/trips/" + tripId;
        }

        return "redirect:/trips";
    }
}