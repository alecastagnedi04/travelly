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

    private final ExpenseRepository expenseRepository;  //salva la spesa
    private final TripRepository tripRepository;  //trova il viaggio
    private final ParticipantRepository participantRepository;  //trova chi ha pagato

    public ExpenseService(
            ExpenseRepository expenseRepository,
            TripRepository tripRepository,
            ParticipantRepository participantRepository) {

        this.expenseRepository = expenseRepository;
        this.tripRepository = tripRepository;
        this.participantRepository = participantRepository;
    }

    public Optional<Expense> createExpense(
            Long tripId,
            String description,
            Double amount,
            LocalDate date,
            ExpenseCategory category,
            Long paidById) {

        Optional<Trip> tripOptional =
                tripRepository.findById(tripId);

        Optional<Participant> participantOptional =
                participantRepository.findById(paidById);

        if (tripOptional.isEmpty() ||
            participantOptional.isEmpty()) {

            return Optional.empty();
        }

        Trip trip = tripOptional.get();
        Participant paidBy = participantOptional.get();

        if (paidBy.getTrip() == null ||
            !paidBy.getTrip().getId().equals(tripId)) {

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