package it.uniroma3.siw.travelly.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import it.uniroma3.siw.travelly.dto.RegistrationForm;
import it.uniroma3.siw.travelly.service.UserService;

import jakarta.validation.Valid;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping("/register")
    public String showRegistrationForm(Model model) {

        model.addAttribute(
                "registrationForm",
                new RegistrationForm()
        );

        return "auth/register";
    }


    @PostMapping("/register")
    public String registerUser(
            @Valid
            @ModelAttribute("registrationForm")
            RegistrationForm registrationForm,

            BindingResult bindingResult) {

        if (userService.usernameExists(
                registrationForm.getUsername())) {

            bindingResult.rejectValue(
                    "username",
                    "duplicate",
                    "Username già utilizzato"
            );
        }


        if (userService.emailExists(
                registrationForm.getEmail())) {

            bindingResult.rejectValue(
                    "email",
                    "duplicate",
                    "Email già utilizzata"
            );
        }


        if (!registrationForm
                .getPassword()
                .equals(
                    registrationForm.getConfirmPassword()
                )) {

            bindingResult.rejectValue(
                    "confirmPassword",
                    "passwordMismatch",
                    "Le password non coincidono"
            );
        }


        if (bindingResult.hasErrors()) {

            return "auth/register";
        }


        userService.registerUser(
                registrationForm.getUsername(),
                registrationForm.getEmail(),
                registrationForm.getPassword()
        );

        return "redirect:/login";
    }
}