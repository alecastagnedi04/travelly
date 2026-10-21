package it.uniroma3.siw.travelly.controller;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import it.uniroma3.siw.travelly.dto.ExpenseDashboardResponse;
import it.uniroma3.siw.travelly.service.ExpenseDashboardService;


@RestController
public class ExpenseDashboardController {

    private final ExpenseDashboardService
            expenseDashboardService;


    public ExpenseDashboardController(
            ExpenseDashboardService
                    expenseDashboardService) {


        this.expenseDashboardService =
                expenseDashboardService;
    }


    /*
     * Endpoint REST utilizzato dal frontend React.
     *
     * Esempio:
     *
     * GET
     * /api/trips/52/expense-dashboard
     */
    @GetMapping(
        "/api/trips/{tripId}/expense-dashboard"
    )
    public ResponseEntity<ExpenseDashboardResponse>
            getExpenseDashboard(

                    @PathVariable("tripId")
                    Long tripId,

                    Principal principal) {


        return expenseDashboardService
                .getDashboard(
                        tripId,
                        principal.getName()
                )
                .map(
                        ResponseEntity::ok
                )
                .orElseGet(
                        () ->
                            ResponseEntity
                                    .notFound()
                                    .build()
                );
    }
}