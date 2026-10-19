package it.uniroma3.siw.travelly.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import it.uniroma3.siw.travelly.service.ParticipantService;
import it.uniroma3.siw.travelly.service.TripService;

@Controller
public class ParticipantController {

    private final ParticipantService participantService;
    private final TripService tripService;


    public ParticipantController(
            ParticipantService participantService,
            TripService tripService) {

        this.participantService = participantService;
        this.tripService = tripService;
    }


    /*
     * Mostra la form solamente se il viaggio
     * appartiene all'utente autenticato.
     */
    @GetMapping("/trips/{tripId}/participants/new")
    public String showCreateForm(
            @PathVariable("tripId") Long tripId,
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

                    return "participants/form";
                })
                .orElse("redirect:/trips");
    }


    /*
     * Salva il partecipante solamente se il viaggio
     * appartiene all'utente autenticato.
     */
    @PostMapping("/trips/{tripId}/participants")
    public String createParticipant(
            @PathVariable("tripId")
            Long tripId,

            @RequestParam("name")
            String name,

            @RequestParam("email")
            String email,

            Principal principal) {

        if (participantService.createParticipant(
                tripId,
                name,
                email,
                principal.getName()
        ).isPresent()) {

            return "redirect:/trips/" + tripId;
        }

        return "redirect:/trips";
    }
}