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

        this.expenseRepository =
                expenseRepository;

        this.tripRepository =
                tripRepository;

        this.participantRepository =
                participantRepository;
    }



    /*
     * =====================================================
     * CREA SPESA MANUALE
     * =====================================================
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


    /*
     * Validazione dati principali.
     */
    if (description == null ||
            description.isBlank()) {

        return Optional.empty();
    }


    if (amount == null ||
            amount <= 0) {

        return Optional.empty();
    }


    if (date == null ||
            category == null) {

        return Optional.empty();
    }


    /*
     * Il viaggio deve appartenere
     * all'utente autenticato.
     */
    Optional<Trip> tripOptional =
            tripRepository
                    .findByIdAndOwnerUsername(
                            tripId,
                            ownerUsername
                    );


    if (tripOptional.isEmpty()) {

        return Optional.empty();
    }


    Trip trip =
            tripOptional.get();


    /*
     * Il pagatore è FACOLTATIVO.
     *
     * Se paidById == null,
     * la spesa nasce come "Da assegnare".
     */
    Participant paidBy =
            null;


    if (paidById != null) {


        Optional<Participant> participantOptional =
                participantRepository
                        .findById(
                                paidById
                        );


        if (participantOptional.isEmpty()) {

            return Optional.empty();
        }


        paidBy =
                participantOptional.get();


        /*
         * Se viene scelto un pagatore,
         * deve appartenere allo stesso viaggio.
         */
        if (paidBy.getTrip() == null ||
                !paidBy
                        .getTrip()
                        .getId()
                        .equals(tripId)) {

            return Optional.empty();
        }
    }


    Expense expense =
            new Expense();


    expense.setDescription(
            description
    );


    expense.setAmount(
            amount
    );


    expense.setDate(
            date
    );


    expense.setCategory(
            category
    );


    expense.setTrip(
            trip
    );


    expense.setPaidBy(
            paidBy
    );


    /*
     * Expense manuale.
     */
    expense.setActivity(
            null
    );


    return Optional.of(
            expenseRepository.save(
                    expense
            )
    );
}



    /*
     * =====================================================
     * ASSEGNA / CAMBIA / RIMUOVE PAGATORE
     * =====================================================
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
                tripRepository
                        .findByIdAndOwnerUsername(
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
                expenseRepository
                        .findById(
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
                !expense
                        .getTrip()
                        .getId()
                        .equals(
                                tripId
                        )) {

            return false;
        }



        /*
         * participantId = null significa:
         *
         * rimuovi il pagatore.
         */
        if (participantId == null) {


            expense.setPaidBy(
                    null
            );


            expenseRepository.save(
                    expense
            );


            return true;
        }



        /*
         * Recuperiamo il partecipante scelto.
         */
        Optional<Participant> participantOptional =
                participantRepository
                        .findById(
                                participantId
                        );


        if (participantOptional.isEmpty()) {

            return false;
        }



        Participant participant =
                participantOptional.get();



        /*
         * Il partecipante deve appartenere
         * allo stesso viaggio della Expense.
         */
        if (participant.getTrip() == null ||
                !participant
                        .getTrip()
                        .getId()
                        .equals(
                                tripId
                        )) {

            return false;
        }



        /*
         * Assegniamo oppure cambiamo pagatore.
         */
        expense.setPaidBy(
                participant
        );


        expenseRepository.save(
                expense
        );


        return true;
    }



    /*
     * =====================================================
     * ELIMINA SPESA MANUALE
     * =====================================================
     */
    @Transactional
    public boolean deleteExpense(
            Long tripId,
            Long expenseId,
            String ownerUsername) {


        /*
         * Il viaggio deve appartenere
         * all'utente autenticato.
         */
        Optional<Trip> tripOptional =
                tripRepository
                        .findByIdAndOwnerUsername(
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
                expenseRepository
                        .findById(
                                expenseId
                        );


        if (expenseOptional.isEmpty()) {

            return false;
        }



        Expense expense =
                expenseOptional.get();



        /*
         * La Expense deve appartenere
         * proprio al viaggio indicato.
         */
        if (expense.getTrip() == null ||
                !expense
                        .getTrip()
                        .getId()
                        .equals(
                                tripId
                        )) {

            return false;
        }



        /*
         * Una Expense generata automaticamente
         * da una Activity NON può essere
         * eliminata direttamente.
         */
        if (expense.getActivity() != null) {

            return false;
        }



        expenseRepository.delete(
                expense
        );


        return true;
    }



    /*
     * =====================================================
     * MODIFICA SPESA MANUALE
     * =====================================================
     */
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
         * =================================================
         * VALIDAZIONE
         * =================================================
         */

        if (description == null ||
                description.isBlank()) {

            return Optional.empty();
        }


        if (amount == null ||
                amount <= 0) {

            return Optional.empty();
        }


        if (date == null ||
                category == null) {

            return Optional.empty();
        }



        /*
         * =================================================
         * CONTROLLO VIAGGIO
         * =================================================
         */

        Optional<Trip> tripOptional =
                tripRepository
                        .findByIdAndOwnerUsername(
                                tripId,
                                ownerUsername
                        );


        if (tripOptional.isEmpty()) {

            return Optional.empty();
        }



        /*
         * =================================================
         * CONTROLLO EXPENSE
         * =================================================
         */

        Optional<Expense> expenseOptional =
                expenseRepository
                        .findById(
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
                !expense
                        .getTrip()
                        .getId()
                        .equals(
                                tripId
                        )) {

            return Optional.empty();
        }



        /*
         * Una Expense generata automaticamente
         * da un'Activity NON può essere
         * modificata direttamente.
         *
         * Va modificata l'Activity.
         */
        if (expense.getActivity() != null) {

            return Optional.empty();
        }



        /*
         * =================================================
         * CONTROLLO PAGATORE
         * =================================================
         *
         * participantId può essere null.
         *
         * In quel caso la spesa rimane
         * temporaneamente senza pagatore.
         */

        Participant participant =
                null;


        if (participantId != null) {


            Optional<Participant> participantOptional =
                    participantRepository
                            .findById(
                                    participantId
                            );


            if (participantOptional.isEmpty()) {

                return Optional.empty();
            }



            participant =
                    participantOptional.get();



            /*
             * Anche in modifica il partecipante
             * deve appartenere allo stesso Trip.
             */
            if (participant.getTrip() == null ||
                    !participant
                            .getTrip()
                            .getId()
                            .equals(
                                    tripId
                            )) {

                return Optional.empty();
            }
        }



        /*
         * =================================================
         * AGGIORNAMENTO
         * =================================================
         */

        expense.setDescription(
                description
        );


        expense.setAmount(
                amount
        );


        expense.setDate(
                date
        );


        expense.setCategory(
                category
        );


        expense.setPaidBy(
                participant
        );


        return Optional.of(
                expenseRepository.save(
                        expense
                )
        );
    }



    /*
     * =====================================================
     * RECUPERA SPESA MANUALE
     * =====================================================
     *
     * Serve per la pagina di modifica.
     *
     * Verifica:
     *
     * - ownership del Trip
     * - appartenenza della Expense al Trip
     * - che la Expense sia manuale
     */
    @Transactional(readOnly = true)
    public Optional<Expense>
            findManualExpenseByIdAndOwnerUsername(
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
                expenseRepository
                        .findById(
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
                !expense
                        .getTrip()
                        .getId()
                        .equals(
                                tripId
                        )) {

            return Optional.empty();
        }



        /*
         * Deve essere una Expense manuale.
         */
        if (expense.getActivity() != null) {

            return Optional.empty();
        }



        return Optional.of(
                expense
        );
    }
}