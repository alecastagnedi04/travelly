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
     * LISTA VIAGGI
     * =====================================================
     *
     * Mostra solamente i viaggi appartenenti
     * all'utente autenticato.
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
     *
     * Crea un nuovo viaggio e lo associa
     * all'utente autenticato.
     *
     * La foto di copertina è facoltativa.
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


        /*
         * Se l'utente non ha caricato una foto,
         * saveTripCover restituisce null.
         */
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
     * DETTAGLIO VIAGGIO
     * =====================================================
     *
     * Mostra il viaggio solamente se appartiene
     * all'utente autenticato.
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
     *
     * Mostra la form solamente se il viaggio
     * appartiene all'utente autenticato.
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
     *
     * Modifica il viaggio solamente se appartiene
     * all'utente autenticato.
     *
     * Se viene caricata una nuova copertina:
     *
     * 1. salviamo la nuova foto
     * 2. eliminiamo quella vecchia
     *
     * Se NON viene caricata una nuova foto,
     * manteniamo quella già presente.
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


            @RequestParam(
                    value = "coverImage",
                    required = false
            )
            MultipartFile coverImage,


            Principal principal) {


        /*
         * Recuperiamo prima il viaggio attuale.
         *
         * Serve sia per controllare la proprietà,
         * sia per conoscere la foto già presente.
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



        /*
         * Per impostazione predefinita
         * manteniamo la vecchia copertina.
         */
        String coverImageName =
                existingTrip.getCoverImage();



        /*
         * Se l'utente ha scelto una nuova immagine,
         * salviamo la nuova copertina.
         */
        if (coverImage != null &&
                !coverImage.isEmpty()) {


            String newCoverImage =
                    imageStorageService
                            .saveTripCover(
                                    coverImage
                            );


            /*
             * Se il salvataggio è riuscito,
             * eliminiamo la vecchia immagine.
             */
            if (newCoverImage != null) {


                imageStorageService
                        .deleteTripCover(
                                existingTrip
                                        .getCoverImage()
                        );


                coverImageName =
                        newCoverImage;
            }
        }



        /*
         * Aggiorniamo il Trip.
         */
        if (tripService
                .updateTrip(
                        id,
                        name,
                        description,
                        startDate,
                        endDate,
                        budget,
                        coverImageName,
                        principal.getName()
                )
                .isPresent()) {


            return "redirect:/trips/" + id;
        }


        return "redirect:/trips";
    }



    /*
     * =====================================================
     * ELIMINA VIAGGIO
     * =====================================================
     *
     * Elimina il viaggio solamente se appartiene
     * all'utente autenticato.
     *
     * Prima di cancellare il Trip,
     * eliminiamo anche l'eventuale immagine
     * presente sul disco.
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



        /*
         * Memorizziamo il nome prima di eliminare
         * il Trip dal database.
         */
        String coverImageName =
                trip.getCoverImage();



        boolean deleted =
                tripService
                        .deleteByIdForOwner(
                                id,
                                principal.getName()
                        );



        /*
         * Eliminiamo il file solamente se
         * il Trip è stato effettivamente cancellato.
         */
        if (deleted) {

            imageStorageService
                    .deleteTripCover(
                            coverImageName
                    );
        }


        return "redirect:/trips";
    }
}