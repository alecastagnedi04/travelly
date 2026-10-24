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
import org.springframework.web.multipart.MultipartFile;

import it.uniroma3.siw.travelly.model.Trip;
import it.uniroma3.siw.travelly.model.TripVisibility;
import it.uniroma3.siw.travelly.service.ImageStorageService;
import it.uniroma3.siw.travelly.service.TripService;


@Controller
public class TripController {


    private final TripService tripService;

    private final ImageStorageService imageStorageService;



    public TripController(
            TripService tripService,
            ImageStorageService imageStorageService) {

        this.tripService =
                tripService;

        this.imageStorageService =
                imageStorageService;
    }



    /*
     * =====================================================
     * LISTA VIAGGI PERSONALI
     * =====================================================
     */
    @GetMapping("/trips")
    public String showTrips(
            Model model,
            Authentication authentication) {


        String username =
                authentication.getName();


        model.addAttribute(
                "trips",
                tripService.findByOwnerUsername(
                        username
                )
        );


        model.addAttribute(
                "username",
                username
        );


        boolean isAdmin =
                authentication
                        .getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority
                                        .getAuthority()
                                        .equals(
                                                "ROLE_ADMIN"
                                        )
                        );


        model.addAttribute(
                "isAdmin",
                isAdmin
        );


        return "trips/list";
    }



    /*
     * =====================================================
     * FORM NUOVO VIAGGIO
     * =====================================================
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
     * =====================================================
     * CREA VIAGGIO
     * =====================================================
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

            @RequestParam(
                    value = "coverImage",
                    required = false
            )
            MultipartFile coverImage,

            Principal principal) {


        String coverImageName =
                imageStorageService
                        .saveTripCover(
                                coverImage
                        );


        tripService.createTrip(
                name,
                description,
                startDate,
                endDate,
                budget,
                coverImageName,
                principal.getName()
        );


        return "redirect:/trips";
    }



    /*
     * =====================================================
     * DETTAGLIO VIAGGIO PERSONALE
     * =====================================================
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
                            tripService
                                    .calculateTotalExpenses(
                                            trip
                                    )
                    );


                    model.addAttribute(
                            "remainingBudget",
                            tripService
                                    .calculateRemainingBudget(
                                            trip
                                    )
                    );


                    model.addAttribute(
                            "username",
                            authentication.getName()
                    );


                    boolean isAdmin =
                            authentication
                                    .getAuthorities()
                                    .stream()
                                    .anyMatch(authority ->
                                            authority
                                                    .getAuthority()
                                                    .equals(
                                                            "ROLE_ADMIN"
                                                    )
                                    );


                    model.addAttribute(
                            "isAdmin",
                            isAdmin
                    );


                    return "trips/show";
                })
                .orElse(
                        "redirect:/trips"
                );
    }



    /*
     * =====================================================
     * FORM MODIFICA
     * =====================================================
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
                .orElse(
                        "redirect:/trips"
                );
    }



    /*
     * =====================================================
     * MODIFICA VIAGGIO
     * =====================================================
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

            @RequestParam("visibility")
            TripVisibility visibility,

            @RequestParam(
                    value = "coverImage",
                    required = false
            )
            MultipartFile coverImage,

            Principal principal) {


        /*
         * Recuperiamo il viaggio esistente
         * per mantenere eventualmente
         * la vecchia copertina.
         */
        Trip existingTrip =
                tripService
                        .findByIdAndOwnerUsername(
                                id,
                                principal.getName()
                        )
                        .orElse(null);


        if (existingTrip == null) {

            return "redirect:/trips";
        }



        String coverImageName =
                existingTrip.getCoverImage();



        /*
         * Se l'utente carica una nuova foto,
         * sostituiamo quella precedente.
         */
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
                .updateTrip(
                        id,
                        name,
                        description,
                        startDate,
                        endDate,
                        budget,
                        coverImageName,
                        visibility,
                        principal.getName()
                )
                .isPresent()) {


            return "redirect:/trips/"
                    + id;
        }


        return "redirect:/trips";
    }



    /*
     * =====================================================
     * ELIMINA VIAGGIO
     * =====================================================
     */
    @PostMapping("/trips/{id}/delete")
    public String deleteTrip(

            @PathVariable("id")
            Long id,

            Principal principal) {


        Trip trip =
                tripService
                        .findByIdAndOwnerUsername(
                                id,
                                principal.getName()
                        )
                        .orElse(null);


        if (trip == null) {

            return "redirect:/trips";
        }



        String coverImage =
                trip.getCoverImage();



        boolean deleted =
                tripService
                        .deleteByIdForOwner(
                                id,
                                principal.getName()
                        );


        if (deleted) {

            imageStorageService
                    .deleteTripCover(
                            coverImage
                    );
        }


        return "redirect:/trips";
    }



    /*
     * =====================================================
     * USA QUESTO ITINERARIO
     * =====================================================
     */
    @PostMapping(
        "/trips/from-template/{templateId}"
    )
    public String createFromTemplate(

            @PathVariable("templateId")
            Long templateId,

            Principal principal) {


        return tripService
                .cloneTemplateForUser(
                        templateId,
                        principal.getName()
                )
                .map(trip ->

                        "redirect:/trips/"
                                + trip.getId()
                )
                .orElse(
                        "redirect:/explore/"
                                + templateId
                );
    }
}