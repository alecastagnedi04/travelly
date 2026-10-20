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
     * Il viaggio deve appartenere
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
            expenseRepository.findById(
                    expenseId
            );


    if (expenseOptional.isEmpty()) {
        return false;
    }


    Expense expense =
            expenseOptional.get();


    /*
     * La Expense deve appartenere
     * proprio al Trip indicato.
     */
    if (expense.getTrip() == null ||
            !expense.getTrip()
                    .getId()
                    .equals(tripId)) {

        return false;
    }


    /*
     * Se participantId è null,
     * vogliamo rimuovere il pagatore.
     */
    if (participantId == null) {

        expense.setPaidBy(null);

        expenseRepository.save(expense);

        return true;
    }


    /*
     * Recuperiamo il partecipante scelto.
     */
    Optional<Participant> participantOptional =
            participantRepository.findById(
                    participantId
            );


    if (participantOptional.isEmpty()) {
        return false;
    }


    Participant participant =
            participantOptional.get();


    /*
     * Sicurezza e consistenza:
     *
     * il partecipante deve appartenere
     * allo stesso viaggio della Expense.
     */
    if (participant.getTrip() == null ||
            !participant.getTrip()
                    .getId()
                    .equals(tripId)) {

        return false;
    }


    /*
     * Assegniamo oppure cambiamo pagatore.
     */
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

@Transactional
public Optional<Expense> updateExpense(
        Long tripId,
        Long expenseId,
        String description,
        Double amount,
        LocalDate date,
        ExpenseCategory category,
        Long participantId,
        String ownerUsername) {


    /*
     * Importo obbligatorio e positivo.
     */
    if (amount == null || amount <= 0) {
        return Optional.empty();
    }


    /*
     * Il viaggio deve appartenere
     * all'utente autenticato.
     */
    Optional<Trip> tripOptional =
            tripRepository.findByIdAndOwnerUsername(
                    tripId,
                    ownerUsername
            );


    if (tripOptional.isEmpty()) {
        return Optional.empty();
    }


    /*
     * Recuperiamo la Expense.
     */
    Optional<Expense> expenseOptional =
            expenseRepository.findById(expenseId);


    if (expenseOptional.isEmpty()) {
        return Optional.empty();
    }


    Expense expense =
            expenseOptional.get();


    /*
     * La Expense deve appartenere
     * proprio al viaggio indicato.
     */
    if (expense.getTrip() == null ||
            !expense.getTrip()
                    .getId()
                    .equals(tripId)) {

        return Optional.empty();
    }


    /*
     * IMPORTANTISSIMO:
     *
     * una Expense generata automaticamente
     * da un'Activity NON può essere modificata
     * direttamente.
     */
    if (expense.getActivity() != null) {
        return Optional.empty();
    }


    /*
     * Se è stato scelto un partecipante,
     * controlliamo che appartenga allo stesso viaggio.
     */
    Participant participant = null;


    if (participantId != null) {

        Optional<Participant> participantOptional =
                participantRepository.findById(
                        participantId
                );


        if (participantOptional.isEmpty()) {
            return Optional.empty();
        }


        participant =
                participantOptional.get();


        if (participant.getTrip() == null ||
                !participant.getTrip()
                        .getId()
                        .equals(tripId)) {

            return Optional.empty();
        }
    }


    /*
     * Aggiornamento della Expense manuale.
     */
    expense.setDescription(description);
    expense.setAmount(amount);
    expense.setDate(date);
    expense.setCategory(category);
    expense.setPaidBy(participant);


    return Optional.of(
            expenseRepository.save(expense)
    );
}

public Optional<Expense> findManualExpenseByIdAndOwnerUsername(
        Long tripId,
        Long expenseId,
        String ownerUsername) {


    /*
     * Il Trip deve appartenere all'utente.
     */
    if (tripRepository
            .findByIdAndOwnerUsername(
                    tripId,
                    ownerUsername
            )
            .isEmpty()) {

        return Optional.empty();
    }


    Optional<Expense> expenseOptional =
            expenseRepository.findById(
                    expenseId
            );


    if (expenseOptional.isEmpty()) {
        return Optional.empty();
    }


    Expense expense =
            expenseOptional.get();


    /*
     * Deve appartenere allo stesso Trip.
     */
    if (expense.getTrip() == null ||
            !expense.getTrip()
                    .getId()
                    .equals(tripId)) {

        return Optional.empty();
    }


    /*
     * Deve essere manuale.
     */
    if (expense.getActivity() != null) {
        return Optional.empty();
    }


    return Optional.of(expense);
}
}