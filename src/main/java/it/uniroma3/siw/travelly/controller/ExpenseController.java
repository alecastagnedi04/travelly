package it.uniroma3.siw.travelly.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import it.uniroma3.siw.travelly.model.ExpenseCategory;
import it.uniroma3.siw.travelly.service.ExpenseService;
import it.uniroma3.siw.travelly.service.TripService;

@Controller
public class ExpenseController {

    private final ExpenseService expenseService;
    private final TripService tripService;

    public ExpenseController(
            ExpenseService expenseService,
            TripService tripService) {

        this.expenseService = expenseService;
        this.tripService = tripService;
    }

    @GetMapping("/trips/{tripId}/expenses/new")
    public String showCreateForm(
            @PathVariable("tripId") Long tripId,
            Model model) {

        return tripService.findById(tripId)
                .map(trip -> {

                    model.addAttribute("trip", trip);

                    model.addAttribute(
                            "participants",
                            trip.getParticipants()
                    );

                    model.addAttribute(
                            "categories",
                            ExpenseCategory.values()
                    );

                    return "expenses/form";
                })
                .orElse("redirect:/trips");
    }

    @PostMapping("/trips/{tripId}/expenses")
    public String createExpense(
            @PathVariable("tripId") Long tripId,
            @RequestParam("description")
            String description,
            @RequestParam("amount")
            Double amount,
            @RequestParam("date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @RequestParam("category")
            ExpenseCategory category,
            @RequestParam("paidById")
            Long paidById) {

        if (expenseService.createExpense(
                tripId,
                description,
                amount,
                date,
                category,
                paidById
        ).isPresent()) {

            return "redirect:/trips/" + tripId;
        }

        return "redirect:/trips";
    }
}