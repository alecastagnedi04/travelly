package it.uniroma3.siw.travelly.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import it.uniroma3.siw.travelly.service.TripService;


@Controller
public class ProfileController {


    @Autowired
    private TripService tripService;


    @GetMapping("/profile")
    public String profile(
            Principal principal,
            Model model) {


        String username =
                principal.getName();


        model.addAttribute(
                "username",
                username
        );


        model.addAttribute(
                "trips",
                tripService.findByOwnerUsername(username)
        );


        return "profile";
    }

}