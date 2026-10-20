package it.uniroma3.siw.travelly.service;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * Creazione manuale di una Expense.
     */
    @Transactional
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


        if (tripOptional.isEmpty()) {
            return Optional.empty();
        }


        Optional<Participant> participantOptional =
                participantRepository.findById(paidById);


        if (participantOptional.isEmpty()) {
            return Optional.empty();
        }


        Trip trip = tripOptional.get();
        Participant paidBy = participantOptional.get();


        /*
         * Il partecipante deve appartenere
         * allo stesso Trip.
         */
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

        /*
         * È una spesa manuale:
         * non deriva da una Activity.
         */
        expense.setActivity(null);


        return Optional.of(
                expenseRepository.save(expense)
        );
    }


    /*
     * Assegna un partecipante come pagatore
     * di una Expense già esistente.
     *
     * È il metodo che utilizzeremo soprattutto
     * per le Expense generate automaticamente
     * dalle Activity.
     */
    @Transactional
    public boolean assignPayer(
            Long tripId,
            Long expenseId,
            Long participantId,
            String ownerUsername) {


        /*
         * Prima controlliamo che il Trip
         * appartenga all'utente autenticato.
         */
        Optional<Trip> tripOptional =
                tripRepository.findByIdAndOwnerUsername(
                        tripId,
                        ownerUsername
                );


        if (tripOptional.isEmpty()) {
            return false;
        }


        Optional<Expense> expenseOptional =
                expenseRepository.findById(expenseId);


        Optional<Participant> participantOptional =
                participantRepository.findById(participantId);


        if (expenseOptional.isEmpty() ||
                participantOptional.isEmpty()) {

            return false;
        }


        Expense expense = expenseOptional.get();
        Participant participant = participantOptional.get();


        /*
         * La Expense deve appartenere al Trip.
         */
        if (expense.getTrip() == null ||
                !expense.getTrip().getId().equals(tripId)) {

            return false;
        }


        /*
         * Anche il Participant deve appartenere
         * allo stesso Trip.
         */
        if (participant.getTrip() == null ||
                !participant.getTrip().getId().equals(tripId)) {

            return false;
        }


        expense.setPaidBy(participant);

        expenseRepository.save(expense);

        return true;
    }

    @Transactional
public boolean deleteExpense(
        Long tripId,
        Long expenseId,
        String ownerUsername) {

    /*
     * Controlliamo prima che il Trip appartenga
     * all'utente autenticato.
     */
    Optional<Trip> tripOptional =
            tripRepository.findByIdAndOwnerUsername(
                    tripId,
                    ownerUsername
            );

    if (tripOptional.isEmpty()) {
        return false;
    }


    /*
     * Recuperiamo la Expense.
     */
    Optional<Expense> expenseOptional =
            expenseRepository.findById(expenseId);

    if (expenseOptional.isEmpty()) {
        return false;
    }


    Expense expense = expenseOptional.get();


    /*
     * La Expense deve appartenere proprio
     * al Trip indicato.
     */
    if (expense.getTrip() == null ||
            !expense.getTrip().getId().equals(tripId)) {

        return false;
    }


    /*
     * Le Expense generate automaticamente
     * dalle Activity NON vengono eliminate qui.
     *
     * In quel caso elimineremo direttamente
     * l'Activity con il metodo già implementato,
     * che cancella anche la relativa Expense.
     */
    if (expense.getActivity() != null) {
        return false;
    }


    expenseRepository.delete(expense);

    return true;
}
}