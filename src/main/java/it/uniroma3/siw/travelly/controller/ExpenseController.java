package it.uniroma3.siw.travelly.controller;

import java.security.Principal;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

        this.expenseService =
                expenseService;

        this.tripService =
                tripService;
    }



    /*
     * =====================================================
     * FORM NUOVA SPESA
     * =====================================================
     */
    @GetMapping("/trips/{tripId}/expenses/new")
    public String showCreateForm(

            @PathVariable("tripId")
            Long tripId,

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
                .orElse(
                        "redirect:/trips"
                );
    }



    /*
     * =====================================================
     * CREA SPESA MANUALE
     * =====================================================
     *
     * Esempi:
     *
     * - hotel
     * - ristorante
     * - volo
     * - taxi
     * - assicurazione
     *
     * Una Expense manuale deve avere
     * un importo maggiore di zero.
     */
    @PostMapping("/trips/{tripId}/expenses")
    public String createExpense(

            @PathVariable("tripId")
            Long tripId,

            @RequestParam("description")
            String description,

            @RequestParam("amount")
            Double amount,

            @RequestParam("date")
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate date,

            @RequestParam("category")
            ExpenseCategory category,

            @RequestParam(
        value = "paidById",
        required = false
)
Long paidById,

            Principal principal,

            RedirectAttributes redirectAttributes) {


        if (expenseService
                .createExpense(
                        tripId,
                        description,
                        amount,
                        date,
                        category,
                        paidById,
                        principal.getName()
                )
                .isPresent()) {


            return "redirect:/trips/"
                    + tripId;
        }



        /*
         * Se qualcosa non è valido,
         * torniamo al form con un messaggio.
         */
        redirectAttributes.addFlashAttribute(
                "expenseError",
                "Spesa non valida. Inserisci una descrizione e un importo maggiore di 0."
        );


        return "redirect:/trips/"
                + tripId
                + "/expenses/new";
    }



    /*
     * =====================================================
     * FORM MODIFICA SPESA
     * =====================================================
     *
     * Possono essere modificate direttamente
     * solamente le Expense manuali.
     *
     * Le Expense generate da Activity
     * devono essere modificate attraverso
     * l'Activity corrispondente.
     */
    @GetMapping(
        "/trips/{tripId}/expenses/{expenseId}/edit"
    )
    public String showEditForm(

            @PathVariable("tripId")
            Long tripId,

            @PathVariable("expenseId")
            Long expenseId,

            Model model,

            Principal principal) {


        return expenseService
                .findManualExpenseByIdAndOwnerUsername(
                        tripId,
                        expenseId,
                        principal.getName()
                )
                .map(expense -> {


                    model.addAttribute(
                            "expense",
                            expense
                    );


                    model.addAttribute(
                            "trip",
                            expense.getTrip()
                    );


                    model.addAttribute(
                            "participants",
                            expense
                                    .getTrip()
                                    .getParticipants()
                    );


                    model.addAttribute(
                            "categories",
                            ExpenseCategory.values()
                    );


                    return "expenses/edit";
                })
                .orElse(
                        "redirect:/trips"
                );
    }



    /*
     * =====================================================
     * MODIFICA SPESA MANUALE
     * =====================================================
     */
    @PostMapping(
        "/trips/{tripId}/expenses/{expenseId}/edit"
    )
    public String updateExpense(

            @PathVariable("tripId")
            Long tripId,

            @PathVariable("expenseId")
            Long expenseId,

            @RequestParam("description")
            String description,

            @RequestParam("amount")
            Double amount,

            @RequestParam("date")
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate date,

            @RequestParam("category")
            ExpenseCategory category,

            @RequestParam(
                    value = "participantId",
                    required = false
            )
            Long participantId,

            Principal principal,

            RedirectAttributes redirectAttributes) {


        if (expenseService
                .updateExpense(
                        tripId,
                        expenseId,
                        description,
                        amount,
                        date,
                        category,
                        participantId,
                        principal.getName()
                )
                .isPresent()) {


            return "redirect:/trips/"
                    + tripId;
        }



        redirectAttributes.addFlashAttribute(
                "expenseError",
                "Modifica non valida. La descrizione non può essere vuota e l'importo deve essere maggiore di 0."
        );


        return "redirect:/trips/"
                + tripId
                + "/expenses/"
                + expenseId
                + "/edit";
    }



    /*
     * =====================================================
     * ASSEGNA / CAMBIA / RIMUOVE PAGATORE
     * =====================================================
     *
     * Vale sia per:
     *
     * - Expense manuali
     * - Expense generate automaticamente da Activity
     */
    @PostMapping(
        "/trips/{tripId}/expenses/{expenseId}/payer"
    )
    public String assignPayer(

            @PathVariable("tripId")
            Long tripId,

            @PathVariable("expenseId")
            Long expenseId,

            @RequestParam(
                    value = "participantId",
                    required = false
            )
            Long participantId,

            Principal principal) {


        expenseService.assignPayer(
                tripId,
                expenseId,
                participantId,
                principal.getName()
        );


        return "redirect:/trips/"
                + tripId;
    }



    /*
     * =====================================================
     * ELIMINA SPESA MANUALE
     * =====================================================
     *
     * Una Expense automatica NON può essere
     * eliminata direttamente.
     *
     * Va eliminata l'Activity che l'ha generata.
     */
    @PostMapping(
        "/trips/{tripId}/expenses/{expenseId}/delete"
    )
    public String deleteExpense(

            @PathVariable("tripId")
            Long tripId,

            @PathVariable("expenseId")
            Long expenseId,

            Principal principal) {


        expenseService.deleteExpense(
                tripId,
                expenseId,
                principal.getName()
        );


        return "redirect:/trips/"
                + tripId;
    }
}