package it.uniroma3.siw.travelly.controller;

import java.security.Principal;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ActivityController {

    private final ActivityService activityService;
    private final TripService tripService;


    public ActivityController(
            ActivityService activityService,
            TripService tripService) {

        this.activityService = activityService;
        this.tripService = tripService;
    }


    /*
     * Form creazione Activity.
     */
    @GetMapping("/trips/{tripId}/activities/new")
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

                    model.addAttribute(
                            "categories",
                            ActivityCategory.values()
                    );

                    return "activities/form";
                })
                .orElse("redirect:/trips");
    }


    /*
     * Creazione Activity.
     */
    @PostMapping("/trips/{tripId}/activities")
public String createActivity(
        @PathVariable("tripId")
        Long tripId,

        @RequestParam("name")
        String name,

        @RequestParam(
                value = "description",
                defaultValue = ""
        )
        String description,

        @RequestParam("date")
        @DateTimeFormat(
                iso = DateTimeFormat.ISO.DATE
        )
        LocalDate date,

        @RequestParam("time")
        @DateTimeFormat(
                iso = DateTimeFormat.ISO.TIME
        )
        LocalTime time,

        @RequestParam("location")
        String location,

        @RequestParam("category")
        ActivityCategory category,

        @RequestParam("cost")
        Double cost,

        Principal principal,

        RedirectAttributes redirectAttributes) {


    if (activityService.createActivity(
            tripId,
            name,
            description,
            date,
            time,
            location,
            category,
            cost,
            principal.getName()
    ).isPresent()) {


        return "redirect:/trips/" + tripId;
    }


    /*
     * L'Activity non è stata creata.
     *
     * Per esempio:
     * - data fuori dal viaggio
     * - costo negativo
     */
    redirectAttributes.addFlashAttribute(
        "activityError",
        "Attività non valida. Verifica che la data sia compresa nelle date del viaggio e che il costo sia maggiore o uguale a 0."
);

    return "redirect:/trips/"
            + tripId
            + "/activities/new";
}


    /*
     * Mostra la form di modifica dell'Activity.
     */
    @GetMapping(
        "/trips/{tripId}/activities/{activityId}/edit"
    )
    public String showEditForm(
            @PathVariable("tripId")
            Long tripId,

            @PathVariable("activityId")
            Long activityId,

            Model model,

            Principal principal) {


        return tripService
                .findByIdAndOwnerUsername(
                        tripId,
                        principal.getName()
                )
                .filter(trip ->
                        trip.getActivities()
                                .stream()
                                .anyMatch(activity ->
                                        activity.getId()
                                                .equals(activityId)
                                )
                )
                .map(trip -> {

                    trip.getActivities()
                            .stream()
                            .filter(activity ->
                                    activity.getId()
                                            .equals(activityId)
                            )
                            .findFirst()
                            .ifPresent(activity ->
                                    model.addAttribute(
                                            "activity",
                                            activity
                                    )
                            );


                    model.addAttribute(
                            "trip",
                            trip
                    );


                    model.addAttribute(
                            "categories",
                            ActivityCategory.values()
                    );


                    return "activities/edit";
                })
                .orElse("redirect:/trips");
    }


    /*
     * Salva le modifiche dell'Activity.
     */
    @PostMapping(
    "/trips/{tripId}/activities/{activityId}/edit"
)
public String updateActivity(
        @PathVariable("tripId")
        Long tripId,

        @PathVariable("activityId")
        Long activityId,

        @RequestParam("name")
        String name,

        @RequestParam(
                value = "description",
                defaultValue = ""
        )
        String description,

        @RequestParam("date")
        @DateTimeFormat(
                iso = DateTimeFormat.ISO.DATE
        )
        LocalDate date,

        @RequestParam("time")
        @DateTimeFormat(
                iso = DateTimeFormat.ISO.TIME
        )
        LocalTime time,

        @RequestParam("location")
        String location,

        @RequestParam("category")
        ActivityCategory category,

        @RequestParam("cost")
        Double cost,

        Principal principal,

        RedirectAttributes redirectAttributes) {


    if (activityService.updateActivity(
            tripId,
            activityId,
            name,
            description,
            date,
            time,
            location,
            category,
            cost,
            principal.getName()
    ).isPresent()) {


        return "redirect:/trips/" + tripId;
    }


    redirectAttributes.addFlashAttribute(
        "activityError",
        "Modifica non valida. Verifica che la data sia compresa nelle date del viaggio e che il costo sia maggiore o uguale a 0."
);


    return "redirect:/trips/"
            + tripId
            + "/activities/"
            + activityId
            + "/edit";
}

    /*
     * Eliminazione Activity.
     */
    @PostMapping(
        "/trips/{tripId}/activities/{activityId}/delete"
    )
    public String deleteActivity(
            @PathVariable("tripId")
            Long tripId,

            @PathVariable("activityId")
            Long activityId,

            Principal principal) {


        activityService.deleteActivity(
                tripId,
                activityId,
                principal.getName()
        );


        return "redirect:/trips/" + tripId;
    }
}