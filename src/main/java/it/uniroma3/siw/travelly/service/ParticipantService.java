package it.uniroma3.siw.travelly.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.travelly.model.Expense;
import it.uniroma3.siw.travelly.model.Participant;
import it.uniroma3.siw.travelly.repository.ExpenseRepository;
import it.uniroma3.siw.travelly.repository.ParticipantRepository;
import it.uniroma3.siw.travelly.repository.TripRepository;

@Service
public class ParticipantService {

    private final ParticipantRepository participantRepository;
    private final TripRepository tripRepository;
    private final ExpenseRepository expenseRepository;


    public ParticipantService(
            ParticipantRepository participantRepository,
            TripRepository tripRepository,
            ExpenseRepository expenseRepository) {

        this.participantRepository = participantRepository;
        this.tripRepository = tripRepository;
        this.expenseRepository = expenseRepository;
    }


    /*
     * Crea un partecipante solamente se
     * il viaggio appartiene all'utente autenticato.
     */
    @Transactional
    public Optional<Participant> createParticipant(
            Long tripId,
            String name,
            String email,
            String ownerUsername) {


        /*
         * Validazione minima.
         */
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }


        return tripRepository
                .findByIdAndOwnerUsername(
                        tripId,
                        ownerUsername
                )
                .map(trip -> {

                    Participant participant =
                            new Participant();

                    participant.setName(name);
                    participant.setEmail(email);
                    participant.setTrip(trip);

                    return participantRepository.save(
                            participant
                    );
                });
    }


    /*
     * Recupera un partecipante solamente se:
     *
     * - il Trip appartiene all'utente autenticato
     * - il Participant appartiene proprio a quel Trip
     */
    public Optional<Participant> findByIdAndOwnerUsername(
            Long tripId,
            Long participantId,
            String ownerUsername) {


        /*
         * Controlliamo prima la proprietà del Trip.
         */
        if (tripRepository
                .findByIdAndOwnerUsername(
                        tripId,
                        ownerUsername
                )
                .isEmpty()) {

            return Optional.empty();
        }


        Optional<Participant> participantOptional =
                participantRepository.findById(
                        participantId
                );


        if (participantOptional.isEmpty()) {
            return Optional.empty();
        }


        Participant participant =
                participantOptional.get();


        /*
         * Il Participant deve appartenere
         * proprio al Trip dell'URL.
         */
        if (participant.getTrip() == null ||
                !participant.getTrip()
                        .getId()
                        .equals(tripId)) {

            return Optional.empty();
        }


        return Optional.of(participant);
    }


    /*
     * Modifica un partecipante.
     */
    @Transactional
    public Optional<Participant> updateParticipant(
            Long tripId,
            Long participantId,
            String name,
            String email,
            String ownerUsername) {


        /*
         * Il nome è obbligatorio.
         */
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }


        /*
         * Il viaggio deve appartenere
         * all'utente autenticato.
         */
        if (tripRepository
                .findByIdAndOwnerUsername(
                        tripId,
                        ownerUsername
                )
                .isEmpty()) {

            return Optional.empty();
        }


        /*
         * Recuperiamo il partecipante.
         */
        Optional<Participant> participantOptional =
                participantRepository.findById(
                        participantId
                );


        if (participantOptional.isEmpty()) {
            return Optional.empty();
        }


        Participant participant =
                participantOptional.get();


        /*
         * Controlliamo che appartenga
         * proprio al Trip indicato.
         */
        if (participant.getTrip() == null ||
                !participant.getTrip()
                        .getId()
                        .equals(tripId)) {

            return Optional.empty();
        }


        /*
         * Aggiorniamo solamente i dati
         * del partecipante.
         *
         * NON cambiamo il Trip.
         */
        participant.setName(name);
        participant.setEmail(email);


        return Optional.of(
                participantRepository.save(participant)
        );
    }


    /*
     * Elimina un partecipante.
     *
     * Le Expense che aveva pagato NON vengono eliminate.
     * Viene solamente rimosso il riferimento paidBy.
     */
    @Transactional
    public boolean deleteParticipant(
            Long tripId,
            Long participantId,
            String ownerUsername) {


        if (tripRepository
                .findByIdAndOwnerUsername(
                        tripId,
                        ownerUsername
                )
                .isEmpty()) {

            return false;
        }


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
         * Controllo di appartenenza.
         */
        if (participant.getTrip() == null ||
                !participant.getTrip()
                        .getId()
                        .equals(tripId)) {

            return false;
        }


        /*
         * Recuperiamo tutte le Expense
         * pagate da questo partecipante.
         */
        Iterable<Expense> expenses =
                expenseRepository.findByPaidById(
                        participantId
                );


        /*
         * Le Expense restano nel viaggio.
         *
         * Viene solamente rimosso il pagatore.
         */
        for (Expense expense : expenses) {

            expense.setPaidBy(null);

            expenseRepository.save(expense);
        }


        participantRepository.delete(participant);


        return true;
    }
}