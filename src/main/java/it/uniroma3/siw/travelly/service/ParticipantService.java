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
     * Crea un partecipante solamente se il viaggio
     * appartiene all'utente autenticato.
     */
    public Optional<Participant> createParticipant(
            Long tripId,
            String name,
            String email,
            String ownerUsername) {

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
     * Elimina un partecipante in sicurezza.
     *
     * Le spese che aveva pagato NON vengono eliminate:
     * il loro paidBy viene semplicemente rimesso a null.
     */
    @Transactional
    public boolean deleteParticipant(
            Long tripId,
            Long participantId,
            String ownerUsername) {


        /*
         * Il Trip deve appartenere
         * all'utente autenticato.
         */
        if (tripRepository
                .findByIdAndOwnerUsername(
                        tripId,
                        ownerUsername
                )
                .isEmpty()) {

            return false;
        }


        /*
         * Recuperiamo il partecipante.
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
         * Il partecipante deve appartenere
         * proprio al Trip indicato.
         */
        if (participant.getTrip() == null ||
                !participant.getTrip()
                        .getId()
                        .equals(tripId)) {

            return false;
        }


        /*
         * Recuperiamo tutte le spese che
         * risultano pagate da questo partecipante.
         */
        Iterable<Expense> expenses =
                expenseRepository.findByPaidById(
                        participantId
                );


        /*
         * Non cancelliamo le spese.
         *
         * Togliamo solamente il riferimento
         * al partecipante che stiamo eliminando.
         */
        for (Expense expense : expenses) {

            expense.setPaidBy(null);

            expenseRepository.save(expense);
        }


        /*
         * Ora possiamo eliminare il partecipante.
         */
        participantRepository.delete(participant);


        return true;
    }
}