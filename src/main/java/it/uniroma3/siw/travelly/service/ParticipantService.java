package it.uniroma3.siw.travelly.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import it.uniroma3.siw.travelly.model.Participant;
import it.uniroma3.siw.travelly.repository.ParticipantRepository;
import it.uniroma3.siw.travelly.repository.TripRepository;

@Service
public class ParticipantService {

    private final ParticipantRepository participantRepository;
    private final TripRepository tripRepository;


    public ParticipantService(
            ParticipantRepository participantRepository,
            TripRepository tripRepository) {

        this.participantRepository = participantRepository;
        this.tripRepository = tripRepository;
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
}