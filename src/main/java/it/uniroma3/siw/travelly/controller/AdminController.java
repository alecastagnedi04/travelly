package it.uniroma3.siw.travelly.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import it.uniroma3.siw.travelly.service.TripService;
import it.uniroma3.siw.travelly.service.UserService;

@Controller
public class AdminController {

    private final UserService userService;
    private final TripService tripService;


    public AdminController(
            UserService userService,
            TripService tripService) {

        this.userService = userService;
        this.tripService = tripService;
    }


    /*
     * Dashboard principale dell'amministratore.
     */
    @GetMapping("/admin")
    public String showAdminDashboard() {

        return "admin/dashboard";
    }


    /*
     * Mostra tutti gli utenti registrati.
     */
    @GetMapping("/admin/users")
    public String showAllUsers(Model model) {

        model.addAttribute(
                "users",
                userService.findAll()
        );

        return "admin/users";
    }


    /*
     * Mostra tutti i viaggi presenti nel sistema,
     * indipendentemente dal proprietario.
     */
    @GetMapping("/admin/trips")
    public String showAllTrips(Model model) {

        model.addAttribute(
                "trips",
                tripService.findAll()
        );

        return "admin/trips";
    }


    /*
     * L'ADMIN può eliminare qualsiasi viaggio.
     */
    @PostMapping("/admin/trips/{id}/delete")
    public String deleteTrip(
            @PathVariable("id") Long id) {

        if (tripService.findById(id).isPresent()) {

            tripService.deleteById(id);
        }

        return "redirect:/admin/trips";
    }
}