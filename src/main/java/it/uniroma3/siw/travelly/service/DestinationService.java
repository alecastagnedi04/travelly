package it.uniroma3.siw.travelly.service;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.travelly.model.Destination;
import it.uniroma3.siw.travelly.repository.DestinationRepository;
import it.uniroma3.siw.travelly.repository.TripRepository;

@Service
public class DestinationService {

    private final DestinationRepository destinationRepository;
    private final TripRepository tripRepository;


    public DestinationService(
            DestinationRepository destinationRepository,
            TripRepository tripRepository) {

        this.destinationRepository = destinationRepository;
        this.tripRepository = tripRepository;
    }


    /*
     * Crea una destinazione solamente se il viaggio
     * appartiene all'utente autenticato.
     */
    public Optional<Destination> createDestination(
            Long tripId,
            String city,
            String country,
            LocalDate arrivalDate,
            LocalDate departureDate,
            String ownerUsername) {

        return tripRepository
                .findByIdAndOwnerUsername(
                        tripId,
                        ownerUsername
                )
                .map(trip -> {

                    Destination destination =
                            new Destination();

                    destination.setCity(city);
                    destination.setCountry(country);
                    destination.setArrivalDate(arrivalDate);
                    destination.setDepartureDate(departureDate);

                    destination.setTrip(trip);

                    return destinationRepository.save(
                            destination
                    );
                });
    }


    /*
     * Elimina una destinazione solamente se:
     *
     * 1. il Trip appartiene all'utente autenticato
     * 2. la Destination esiste
     * 3. la Destination appartiene proprio a quel Trip
     */
    @Transactional
    public boolean deleteDestination(
            Long tripId,
            Long destinationId,
            String ownerUsername) {


        if (tripRepository
                .findByIdAndOwnerUsername(
                        tripId,
                        ownerUsername
                )
                .isEmpty()) {

            return false;
        }


        Optional<Destination> destinationOptional =
                destinationRepository.findById(destinationId);


        if (destinationOptional.isEmpty()) {
            return false;
        }


        Destination destination =
                destinationOptional.get();


        if (destination.getTrip() == null ||
                !destination.getTrip()
                        .getId()
                        .equals(tripId)) {

            return false;
        }


        destinationRepository.delete(destination);

        return true;
    }
}