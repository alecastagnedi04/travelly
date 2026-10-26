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


        this.participantRepository =
                participantRepository;

        this.tripRepository =
                tripRepository;

        this.expenseRepository =
                expenseRepository;
    }



    /*
     * =====================================================
     * CREA PARTECIPANTE
     * =====================================================
     */
    @Transactional
    public Optional<Participant> createParticipant(
            Long tripId,
            String name,
            String email,
            String ownerUsername) {


        /*
         * Nome obbligatorio.
         */
        if (name == null ||
                name.isBlank()) {

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


                    participant.setName(
                            name
                    );


                    participant.setEmail(
                            email
                    );


                    participant.setTrip(
                            trip
                    );


                    return participantRepository.save(
                            participant
                    );
                });
    }



    /*
     * =====================================================
     * RECUPERA PARTECIPANTE
     * =====================================================
     *
     * Metodo di sola lettura.
     */
    @Transactional(readOnly = true)
    public Optional<Participant> findByIdAndOwnerUsername(
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

            return Optional.empty();
        }



        Optional<Participant> participantOptional =
                participantRepository
                        .findById(
                                participantId
                        );


        if (participantOptional.isEmpty()) {

            return Optional.empty();
        }



        Participant participant =
                participantOptional.get();



        /*
         * Il Participant deve appartenere
         * realmente al viaggio indicato.
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



        return Optional.of(
                participant
        );
    }



    /*
     * =====================================================
     * MODIFICA PARTECIPANTE
     * =====================================================
     */
    @Transactional
    public Optional<Participant> updateParticipant(
            Long tripId,
            Long participantId,
            String name,
            String email,
            String ownerUsername) {


        if (name == null ||
                name.isBlank()) {

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



        Optional<Participant> participantOptional =
                participantRepository
                        .findById(
                                participantId
                        );


        if (participantOptional.isEmpty()) {

            return Optional.empty();
        }



        Participant participant =
                participantOptional.get();



        /*
         * Deve appartenere al Trip indicato.
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



        participant.setName(
                name
        );


        participant.setEmail(
                email
        );



        return Optional.of(
                participantRepository.save(
                        participant
                )
        );
    }



    /*
     * =====================================================
     * ELIMINA PARTECIPANTE
     * =====================================================
     *
     * IMPORTANTE:
     *
     * le Expense NON vengono eliminate.
     *
     * Se il Participant era il pagatore,
     * paidBy viene semplicemente impostato a null.
     */
    @Transactional
    public boolean deleteParticipant(
            Long tripId,
            Long participantId,
            String ownerUsername) {


        /*
         * Controllo proprietà del Trip.
         */
        if (tripRepository
                .findByIdAndOwnerUsername(
                        tripId,
                        ownerUsername
                )
                .isEmpty()) {

            return false;
        }



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
         * Controllo appartenenza al viaggio.
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
         * Recuperiamo tutte le Expense
         * pagate dal partecipante.
         */
        Iterable<Expense> expenses =
                expenseRepository
                        .findByPaidById(
                                participantId
                        );



        /*
         * Le Expense rimangono.
         *
         * Il pagatore diventa "Da assegnare".
         */
        for (Expense expense : expenses) {


            expense.setPaidBy(
                    null
            );


            expenseRepository.save(
                    expense
            );
        }



        participantRepository.delete(
                participant
        );


        return true;
    }
}