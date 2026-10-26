package it.uniroma3.siw.travelly.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import it.uniroma3.siw.travelly.model.Trip;
import it.uniroma3.siw.travelly.service.ImageStorageService;
import it.uniroma3.siw.travelly.service.TripService;
import it.uniroma3.siw.travelly.service.UserService;


@Controller
public class AdminController {


    private final UserService userService;

    private final TripService tripService;

    private final ImageStorageService imageStorageService;



    public AdminController(
            UserService userService,
            TripService tripService,
            ImageStorageService imageStorageService) {


        this.userService =
                userService;


        this.tripService =
                tripService;


        this.imageStorageService =
                imageStorageService;
    }



    /*
     * =====================================================
     * DASHBOARD ADMIN
     * =====================================================
     */
    @GetMapping("/admin")
    public String showAdminDashboard() {


        return "admin/dashboard";
    }



    /*
     * =====================================================
     * LISTA UTENTI
     * =====================================================
     */
    @GetMapping("/admin/users")
    public String showAllUsers(
            Model model) {


        model.addAttribute(
                "users",
                userService.findAll()
        );


        return "admin/users";
    }



    /*
     * =====================================================
     * LISTA DI TUTTI I VIAGGI
     * =====================================================
     *
     * L'amministratore può vedere:
     *
     * - viaggi personali
     * - viaggi pubblici
     * - viaggi privati
     * - template ufficiali
     */
    @GetMapping("/admin/trips")
    public String showAllTrips(
            Model model) {


        model.addAttribute(
                "trips",
                tripService.findAll()
        );


        return "admin/trips";
    }



    /*
     * =====================================================
     * ELIMINA VIAGGIO
     * =====================================================
     *
     * L'ADMIN può eliminare qualsiasi Trip.
     *
     * Prima recuperiamo il Trip per ricordare
     * il nome della copertina.
     *
     * Dopo l'eliminazione dal database,
     * eliminiamo anche il relativo file
     * presente in uploads/trips.
     */
    @PostMapping("/admin/trips/{id}/delete")
    public String deleteTrip(
            @PathVariable("id")
            Long id) {


        /*
         * Recuperiamo il viaggio.
         */
        Trip trip =
                tripService
                        .findById(
                                id
                        )
                        .orElse(null);



        /*
         * Trip inesistente.
         */
        if (trip == null) {


            return "redirect:/admin/trips";
        }



        /*
         * Salviamo il nome della copertina
         * PRIMA di eliminare il Trip.
         */
        String coverImage =
                trip.getCoverImage();



        /*
         * Eliminiamo il Trip dal database.
         *
         * Grazie a CascadeType.ALL e orphanRemoval,
         * vengono eliminate anche le entità figlie
         * associate al Trip.
         */
        tripService.deleteById(
                id
        );



        /*
         * Eliminiamo anche il file fisico
         * della copertina.
         *
         * Se coverImage è null,
         * ImageStorageService non fa nulla.
         */
        imageStorageService
                .deleteTripCover(
                        coverImage
                );



        return "redirect:/admin/trips";
    }
}