package it.uniroma3.siw.travelly.service;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.stereotype.Service;

import it.uniroma3.siw.travelly.model.Expense;
import it.uniroma3.siw.travelly.model.ExpenseCategory;
import it.uniroma3.siw.travelly.model.Participant;
import it.uniroma3.siw.travelly.model.Trip;
import it.uniroma3.siw.travelly.repository.ExpenseRepository;
import it.uniroma3.siw.travelly.repository.ParticipantRepository;
import it.uniroma3.siw.travelly.repository.TripRepository;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final TripRepository tripRepository;
    private final ParticipantRepository participantRepository;


    public ExpenseService(
            ExpenseRepository expenseRepository,
            TripRepository tripRepository,
            ParticipantRepository participantRepository) {

        this.expenseRepository = expenseRepository;
        this.tripRepository = tripRepository;
        this.participantRepository = participantRepository;
    }


    /*
     * Crea una spesa solamente se:
     *
     * 1. il viaggio appartiene all'utente autenticato
     * 2. il partecipante esiste
     * 3. il partecipante appartiene allo stesso viaggio
     */
    public Optional<Expense> createExpense(
            Long tripId,
            String description,
            Double amount,
            LocalDate date,
            ExpenseCategory category,
            Long paidById,
            String ownerUsername) {

        Optional<Trip> tripOptional =
                tripRepository.findByIdAndOwnerUsername(
                        tripId,
                        ownerUsername
                );

        /*
         * Se il viaggio non esiste oppure non appartiene
         * all'utente autenticato, fermiamo tutto.
         */
        if (tripOptional.isEmpty()) {
            return Optional.empty();
        }

        Optional<Participant> participantOptional =
                participantRepository.findById(paidById);

        /*
         * Anche il partecipante deve esistere.
         */
        if (participantOptional.isEmpty()) {
            return Optional.empty();
        }

        Trip trip = tripOptional.get();
        Participant paidBy = participantOptional.get();


        /*
         * Verifichiamo che il partecipante appartenga
         * proprio al viaggio su cui stiamo creando la spesa.
         */
        if (paidBy.getTrip() == null ||
                !paidBy.getTrip().getId().equals(trip.getId())) {

            return Optional.empty();
        }


        Expense expense = new Expense();

        expense.setDescription(description);
        expense.setAmount(amount);
        expense.setDate(date);
        expense.setCategory(category);

        expense.setTrip(trip);
        expense.setPaidBy(paidBy);

        return Optional.of(
                expenseRepository.save(expense)
        );
    }
}