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
import org.springframework.web.multipart.MultipartFile;

import it.uniroma3.siw.travelly.model.Activity;
import it.uniroma3.siw.travelly.model.ActivityCategory;
import it.uniroma3.siw.travelly.model.Destination;
import it.uniroma3.siw.travelly.model.Trip;
import it.uniroma3.siw.travelly.service.ActivityService;
import it.uniroma3.siw.travelly.service.DestinationService;
import it.uniroma3.siw.travelly.service.ImageStorageService;
import it.uniroma3.siw.travelly.service.TripService;


@Controller
public class AdminTemplateController {


    private final TripService tripService;

    private final ImageStorageService imageStorageService;

    private final DestinationService destinationService;

    private final ActivityService activityService;



    public AdminTemplateController(
            TripService tripService,
            ImageStorageService imageStorageService,
            DestinationService destinationService,
            ActivityService activityService) {

        this.tripService =
                tripService;

        this.imageStorageService =
                imageStorageService;

        this.destinationService =
                destinationService;

        this.activityService =
                activityService;
    }



    /*
     * =====================================================
     * LISTA ITINERARI
     * =====================================================
     */
    @GetMapping("/admin/templates")
    public String showTemplates(
            Model model) {


        model.addAttribute(
                "templates",
                tripService.findTemplates()
        );


        return "admin/templates";
    }



    /*
     * =====================================================
     * DETTAGLIO ITINERARIO
     * =====================================================
     */
    @GetMapping("/admin/templates/{id}")
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


                    return "admin/template-detail";
                })
                .orElse(
                        "redirect:/admin/templates"
                );
    }



    /*
     * =====================================================
     * FORM NUOVO TEMPLATE
     * =====================================================
     */
    @GetMapping("/admin/templates/new")
    public String showCreateForm() {

        return "admin/template-form";
    }



    /*
     * =====================================================
     * CREA TEMPLATE
     * =====================================================
     */
    @PostMapping("/admin/templates")
    public String createTemplate(

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

            @RequestParam(
                    value = "coverImage",
                    required = false
            )
            MultipartFile coverImage) {


        String coverImageName =
                imageStorageService
                        .saveTripCover(
                                coverImage
                        );


        Trip trip =
                tripService
                        .createTemplate(
                                name,
                                description,
                                startDate,
                                endDate,
                                budget,
                                coverImageName
                        );


        return "redirect:/admin/templates/"
                + trip.getId();
    }



    /*
     * =====================================================
     * FORM MODIFICA TEMPLATE
     * =====================================================
     */
    @GetMapping("/admin/templates/{id}/edit")
    public String showEditForm(
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


                    return "admin/template-edit";
                })
                .orElse(
                        "redirect:/admin/templates"
                );
    }



    /*
     * =====================================================
     * MODIFICA TEMPLATE
     * =====================================================
     */
    @PostMapping("/admin/templates/{id}/edit")
    public String updateTemplate(

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

            @RequestParam(
                    value = "coverImage",
                    required = false
            )
            MultipartFile coverImage) {


        Trip existingTrip =
                tripService
                        .findTemplateById(
                                id
                        )
                        .orElse(null);


        if (existingTrip == null) {

            return "redirect:/admin/templates";
        }


        String coverImageName =
                existingTrip.getCoverImage();


        if (coverImage != null &&
                !coverImage.isEmpty()) {


            String newImage =
                    imageStorageService
                            .saveTripCover(
                                    coverImage
                            );


            if (newImage != null) {


                imageStorageService
                        .deleteTripCover(
                                existingTrip
                                        .getCoverImage()
                        );


                coverImageName =
                        newImage;
            }
        }


        if (tripService
                .updateTemplate(
                        id,
                        name,
                        description,
                        startDate,
                        endDate,
                        budget,
                        coverImageName
                )
                .isPresent()) {


            return "redirect:/admin/templates/"
                    + id;
        }


        return "redirect:/admin/templates";
    }



    /*
     * =====================================================
     * ELIMINA TEMPLATE
     * =====================================================
     */
    @PostMapping("/admin/templates/{id}/delete")
    public String deleteTemplate(
            @PathVariable("id")
            Long id) {


        Trip trip =
                tripService
                        .findTemplateById(
                                id
                        )
                        .orElse(null);


        if (trip == null) {

            return "redirect:/admin/templates";
        }


        String coverImage =
                trip.getCoverImage();


        boolean deleted =
                tripService
                        .deleteTemplate(
                                id
                        );


        if (deleted) {

            imageStorageService
                    .deleteTripCover(
                            coverImage
                    );
        }


        return "redirect:/admin/templates";
    }



    /*
     * =====================================================
     * NUOVA DESTINAZIONE
     * =====================================================
     */
    @GetMapping(
        "/admin/templates/{templateId}/destinations/new"
    )
    public String showDestinationForm(

            @PathVariable("templateId")
            Long templateId,

            Model model) {


        Trip trip =
                tripService
                        .findTemplateById(
                                templateId
                        )
                        .orElse(null);


        if (trip == null) {

            return "redirect:/admin/templates";
        }


        model.addAttribute(
                "trip",
                trip
        );


        return "admin/template-destination-form";
    }



    @PostMapping(
        "/admin/templates/{templateId}/destinations"
    )
    public String createDestination(

            @PathVariable("templateId")
            Long templateId,

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
            LocalDate departureDate) {


        destinationService
                .createTemplateDestination(
                        templateId,
                        city,
                        country,
                        arrivalDate,
                        departureDate
                );


        return "redirect:/admin/templates/"
                + templateId;
    }



    /*
     * =====================================================
     * MODIFICA DESTINAZIONE
     * =====================================================
     */
    @GetMapping(
        "/admin/templates/{templateId}/destinations/{destinationId}/edit"
    )
    public String showDestinationEditForm(

            @PathVariable("templateId")
            Long templateId,

            @PathVariable("destinationId")
            Long destinationId,

            Model model) {


        Trip trip =
                tripService
                        .findTemplateById(
                                templateId
                        )
                        .orElse(null);


        Destination destination =
                destinationService
                        .findTemplateDestination(
                                templateId,
                                destinationId
                        )
                        .orElse(null);


        if (trip == null ||
                destination == null) {

            return "redirect:/admin/templates/"
                    + templateId;
        }


        model.addAttribute(
                "trip",
                trip
        );


        model.addAttribute(
                "destination",
                destination
        );


        return "admin/template-destination-edit";
    }



    @PostMapping(
        "/admin/templates/{templateId}/destinations/{destinationId}/edit"
    )
    public String updateDestination(

            @PathVariable("templateId")
            Long templateId,

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
            LocalDate departureDate) {


        destinationService
                .updateTemplateDestination(
                        templateId,
                        destinationId,
                        city,
                        country,
                        arrivalDate,
                        departureDate
                );


        return "redirect:/admin/templates/"
                + templateId;
    }



    /*
     * =====================================================
     * ELIMINA DESTINAZIONE
     * =====================================================
     */
    @PostMapping(
        "/admin/templates/{templateId}/destinations/{destinationId}/delete"
    )
    public String deleteDestination(

            @PathVariable("templateId")
            Long templateId,

            @PathVariable("destinationId")
            Long destinationId) {


        destinationService
                .deleteTemplateDestination(
                        templateId,
                        destinationId
                );


        return "redirect:/admin/templates/"
                + templateId;
    }



    /*
     * =====================================================
     * NUOVA ATTIVITÀ
     * =====================================================
     */
    @GetMapping(
        "/admin/templates/{templateId}/activities/new"
    )
    public String showActivityForm(

            @PathVariable("templateId")
            Long templateId,

            Model model) {


        Trip trip =
                tripService
                        .findTemplateById(
                                templateId
                        )
                        .orElse(null);


        if (trip == null) {

            return "redirect:/admin/templates";
        }


        model.addAttribute(
                "trip",
                trip
        );


        model.addAttribute(
                "categories",
                ActivityCategory.values()
        );


        return "admin/template-activity-form";
    }



    @PostMapping(
        "/admin/templates/{templateId}/activities"
    )
    public String createActivity(

            @PathVariable("templateId")
            Long templateId,

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
            Double cost) {


        activityService
                .createTemplateActivity(
                        templateId,
                        name,
                        description,
                        date,
                        time,
                        location,
                        category,
                        cost
                );


        return "redirect:/admin/templates/"
                + templateId;
    }



    /*
     * =====================================================
     * MODIFICA ATTIVITÀ
     * =====================================================
     */
    @GetMapping(
        "/admin/templates/{templateId}/activities/{activityId}/edit"
    )
    public String showActivityEditForm(

            @PathVariable("templateId")
            Long templateId,

            @PathVariable("activityId")
            Long activityId,

            Model model) {


        Trip trip =
                tripService
                        .findTemplateById(
                                templateId
                        )
                        .orElse(null);


        Activity activity =
                activityService
                        .findTemplateActivity(
                                templateId,
                                activityId
                        )
                        .orElse(null);


        if (trip == null ||
                activity == null) {

            return "redirect:/admin/templates/"
                    + templateId;
        }


        model.addAttribute(
                "trip",
                trip
        );


        model.addAttribute(
                "activity",
                activity
        );


        model.addAttribute(
                "categories",
                ActivityCategory.values()
        );


        return "admin/template-activity-edit";
    }



    @PostMapping(
        "/admin/templates/{templateId}/activities/{activityId}/edit"
    )
    public String updateActivity(

            @PathVariable("templateId")
            Long templateId,

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
            Double cost) {


        activityService
                .updateTemplateActivity(
                        templateId,
                        activityId,
                        name,
                        description,
                        date,
                        time,
                        location,
                        category,
                        cost
                );


        return "redirect:/admin/templates/"
                + templateId;
    }



    /*
     * =====================================================
     * ELIMINA ATTIVITÀ
     * =====================================================
     */
    @PostMapping(
        "/admin/templates/{templateId}/activities/{activityId}/delete"
    )
    public String deleteActivity(

            @PathVariable("templateId")
            Long templateId,

            @PathVariable("activityId")
            Long activityId) {


        activityService
                .deleteTemplateActivity(
                        templateId,
                        activityId
                );


        return "redirect:/admin/templates/"
                + templateId;
    }
}