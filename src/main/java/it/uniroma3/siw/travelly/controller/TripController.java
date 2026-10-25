package it.uniroma3.siw.travelly.controller;

import java.security.Principal;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import it.uniroma3.siw.travelly.dto.TripForm;
import it.uniroma3.siw.travelly.model.Trip;
import it.uniroma3.siw.travelly.model.TripVisibility;
import it.uniroma3.siw.travelly.service.ImageStorageService;
import it.uniroma3.siw.travelly.service.TripService;

import jakarta.validation.Valid;


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


        model.addAttribute(
                "tripForm",
                new TripForm()
        );


        return "trips/form";
    }



    /*
     * =====================================================
     * CREA NUOVO VIAGGIO
     * =====================================================
     */
    @PostMapping("/trips")
    public String createTrip(

            @Valid
            @ModelAttribute("tripForm")
            TripForm tripForm,

            BindingResult bindingResult,

            @RequestParam(
                    value = "coverImage",
                    required = false
            )
            MultipartFile coverImage,

            Principal principal,

            Model model) {


        /*
         * La data di ritorno non può essere
         * precedente alla partenza.
         */
        if (tripForm.getStartDate() != null &&
                tripForm.getEndDate() != null &&
                tripForm
                        .getEndDate()
                        .isBefore(
                                tripForm.getStartDate()
                        )) {


            bindingResult.rejectValue(
                    "endDate",
                    "trip.endDate.beforeStart",
                    "La data di ritorno non può precedere la data di partenza"
            );
        }



        if (bindingResult.hasErrors()) {


            model.addAttribute(
                    "username",
                    principal.getName()
            );


            return "trips/form";
        }



        /*
         * Salviamo la foto solamente
         * dopo la validazione.
         */
        String coverImageName =
                imageStorageService
                        .saveTripCover(
                                coverImage
                        );



        tripService.createTrip(
                tripForm.getName(),
                tripForm.getDescription(),
                tripForm.getStartDate(),
                tripForm.getEndDate(),
                tripForm.getBudget(),
                coverImageName,
                principal.getName()
        );


        return "redirect:/trips";
    }



    /*
     * =====================================================
     * DETTAGLIO VIAGGIO
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
     * FORM MODIFICA VIAGGIO
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


                    /*
                     * Creiamo il form partendo dai dati
                     * già salvati nel Trip.
                     */
                    TripForm tripForm =
                            new TripForm();



                    tripForm.setName(
                            trip.getName()
                    );


                    tripForm.setDescription(
                            trip.getDescription()
                    );



                    /*
                     * IMPORTANTE:
                     *
                     * Le date già presenti nel database
                     * vengono copiate nel TripForm.
                     *
                     * In questo modo gli input type="date"
                     * saranno già compilati.
                     */
                    tripForm.setStartDate(
                            trip.getStartDate()
                    );


                    tripForm.setEndDate(
                            trip.getEndDate()
                    );



                    tripForm.setBudget(
                            trip.getBudget()
                    );



                    /*
                     * Manteniamo anche la visibilità
                     * attualmente salvata.
                     */
                    if (trip.getVisibility() != null) {


                        tripForm.setVisibility(
                                trip.getVisibility()
                        );


                    } else {


                        tripForm.setVisibility(
                                TripVisibility.PRIVATE
                        );
                    }



                    model.addAttribute(
                            "trip",
                            trip
                    );


                    model.addAttribute(
                            "tripForm",
                            tripForm
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
     * SALVA MODIFICA VIAGGIO
     * =====================================================
     */
    @PostMapping("/trips/{id}/edit")
    public String updateTrip(

            @PathVariable("id")
            Long id,

            @Valid
            @ModelAttribute("tripForm")
            TripForm tripForm,

            BindingResult bindingResult,

            @RequestParam("visibility")
            TripVisibility visibility,

            @RequestParam(
                    value = "coverImage",
                    required = false
            )
            MultipartFile coverImage,

            Principal principal,

            Model model) {


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



        /*
         * Salviamo esplicitamente
         * la visibility ricevuta dal form.
         */
        tripForm.setVisibility(
                visibility
        );



        /*
         * Validazione intervallo date.
         */
        if (tripForm.getStartDate() != null &&
                tripForm.getEndDate() != null &&
                tripForm
                        .getEndDate()
                        .isBefore(
                                tripForm.getStartDate()
                        )) {


            bindingResult.rejectValue(
                    "endDate",
                    "trip.endDate.beforeStart",
                    "La data di ritorno non può precedere la data di partenza"
            );
        }



        /*
         * Se il form contiene errori,
         * torniamo alla stessa pagina.
         *
         * I valori inseriti restano nel TripForm.
         */
        if (bindingResult.hasErrors()) {


            model.addAttribute(
                    "trip",
                    existingTrip
            );


            model.addAttribute(
                    "username",
                    principal.getName()
            );


            return "trips/edit";
        }



        /*
         * Manteniamo la copertina esistente
         * finché non ne viene caricata una nuova.
         */
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



        /*
         * Salvataggio effettivo.
         */
        if (tripService
                .updateTrip(
                        id,
                        tripForm.getName(),
                        tripForm.getDescription(),
                        tripForm.getStartDate(),
                        tripForm.getEndDate(),
                        tripForm.getBudget(),
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
     * USA ITINERARIO UFFICIALE
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