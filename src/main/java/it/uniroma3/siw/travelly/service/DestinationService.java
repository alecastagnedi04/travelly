package it.uniroma3.siw.travelly.service;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.travelly.model.Destination;
import it.uniroma3.siw.travelly.model.Trip;
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
     * Crea una destinazione solamente se:
     *
     * - il viaggio appartiene all'utente autenticato
     * - le date sono valide
     */
    @Transactional
    public Optional<Destination> createDestination(
            Long tripId,
            String city,
            String country,
            LocalDate arrivalDate,
            LocalDate departureDate,
            String ownerUsername) {


        Optional<Trip> tripOptional =
                tripRepository.findByIdAndOwnerUsername(
                        tripId,
                        ownerUsername
                );


        if (tripOptional.isEmpty()) {
            return Optional.empty();
        }


        Trip trip = tripOptional.get();


        if (!datesAreValid(
                trip,
                arrivalDate,
                departureDate)) {

            return Optional.empty();
        }


        Destination destination =
                new Destination();


        destination.setCity(city);
        destination.setCountry(country);
        destination.setArrivalDate(arrivalDate);
        destination.setDepartureDate(departureDate);
        destination.setTrip(trip);


        return Optional.of(
                destinationRepository.save(destination)
        );
    }


    /*
     * Recupera una Destination solamente se
     * appartiene a un viaggio dell'utente autenticato.
     */
    public Optional<Destination> findByIdAndOwnerUsername(
            Long tripId,
            Long destinationId,
            String ownerUsername) {


        /*
         * Prima verifichiamo la proprietà del Trip.
         */
        if (tripRepository
                .findByIdAndOwnerUsername(
                        tripId,
                        ownerUsername
                )
                .isEmpty()) {

            return Optional.empty();
        }


        Optional<Destination> destinationOptional =
                destinationRepository.findById(
                        destinationId
                );


        if (destinationOptional.isEmpty()) {
            return Optional.empty();
        }


        Destination destination =
                destinationOptional.get();


        /*
         * La Destination deve appartenere
         * proprio al Trip indicato.
         */
        if (destination.getTrip() == null ||
                !destination.getTrip()
                        .getId()
                        .equals(tripId)) {

            return Optional.empty();
        }


        return Optional.of(destination);
    }


    /*
     * Modifica una Destination solamente se:
     *
     * - il Trip appartiene all'utente autenticato
     * - la Destination appartiene a quel Trip
     * - le nuove date sono valide
     */
    @Transactional
    public Optional<Destination> updateDestination(
            Long tripId,
            Long destinationId,
            String city,
            String country,
            LocalDate arrivalDate,
            LocalDate departureDate,
            String ownerUsername) {


        Optional<Trip> tripOptional =
                tripRepository.findByIdAndOwnerUsername(
                        tripId,
                        ownerUsername
                );


        if (tripOptional.isEmpty()) {
            return Optional.empty();
        }


        Trip trip =
                tripOptional.get();


        /*
         * Controlliamo la coerenza delle date.
         */
        if (!datesAreValid(
                trip,
                arrivalDate,
                departureDate)) {

            return Optional.empty();
        }


        Optional<Destination> destinationOptional =
                destinationRepository.findById(
                        destinationId
                );


        if (destinationOptional.isEmpty()) {
            return Optional.empty();
        }


        Destination destination =
                destinationOptional.get();


        /*
         * Sicurezza:
         *
         * la Destination deve appartenere
         * proprio al Trip indicato nell'URL.
         */
        if (destination.getTrip() == null ||
                !destination.getTrip()
                        .getId()
                        .equals(tripId)) {

            return Optional.empty();
        }


        destination.setCity(city);
        destination.setCountry(country);
        destination.setArrivalDate(arrivalDate);
        destination.setDepartureDate(departureDate);


        return Optional.of(
                destinationRepository.save(destination)
        );
    }


    /*
     * Elimina una Destination solamente se:
     *
     * - il Trip appartiene all'utente autenticato
     * - la Destination appartiene a quel Trip
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
                destinationRepository.findById(
                        destinationId
                );


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


/*
 * =====================================================
 * DESTINAZIONI DEGLI ITINERARI TRAVELLY
 * =====================================================
 */


/*
 * Crea una destinazione dentro
 * un itinerario ufficiale Travelly.
 */
@Transactional
public Optional<Destination> createTemplateDestination(
        Long templateId,
        String city,
        String country,
        LocalDate arrivalDate,
        LocalDate departureDate) {


    Optional<Trip> tripOptional =
            tripRepository
                    .findByIdAndTemplateTrue(
                            templateId
                    );


    if (tripOptional.isEmpty()) {

        return Optional.empty();
    }


    Trip trip =
            tripOptional.get();


    if (!datesAreValid(
            trip,
            arrivalDate,
            departureDate)) {

        return Optional.empty();
    }


    Destination destination =
            new Destination();


    destination.setCity(
            city
    );


    destination.setCountry(
            country
    );


    destination.setArrivalDate(
            arrivalDate
    );


    destination.setDepartureDate(
            departureDate
    );


    destination.setTrip(
            trip
    );


    return Optional.of(
            destinationRepository.save(
                    destination
            )
    );
}


/*
 * Recupera una destinazione verificando
 * che appartenga proprio al template indicato.
 */
@Transactional(readOnly = true)
public Optional<Destination> findTemplateDestination(
        Long templateId,
        Long destinationId) {


    if (tripRepository
            .findByIdAndTemplateTrue(
                    templateId
            )
            .isEmpty()) {

        return Optional.empty();
    }


    Optional<Destination> destinationOptional =
            destinationRepository.findById(
                    destinationId
            );


    if (destinationOptional.isEmpty()) {

        return Optional.empty();
    }


    Destination destination =
            destinationOptional.get();


    if (destination.getTrip() == null ||
            !destination.getTrip()
                    .getId()
                    .equals(templateId)) {

        return Optional.empty();
    }


    return Optional.of(
            destination
    );
}


/*
 * Modifica una destinazione
 * di un itinerario Travelly.
 */
@Transactional
public Optional<Destination> updateTemplateDestination(
        Long templateId,
        Long destinationId,
        String city,
        String country,
        LocalDate arrivalDate,
        LocalDate departureDate) {


    Optional<Trip> tripOptional =
            tripRepository
                    .findByIdAndTemplateTrue(
                            templateId
                    );


    if (tripOptional.isEmpty()) {

        return Optional.empty();
    }


    Trip trip =
            tripOptional.get();


    if (!datesAreValid(
            trip,
            arrivalDate,
            departureDate)) {

        return Optional.empty();
    }


    Optional<Destination> destinationOptional =
            findTemplateDestination(
                    templateId,
                    destinationId
            );


    if (destinationOptional.isEmpty()) {

        return Optional.empty();
    }


    Destination destination =
            destinationOptional.get();


    destination.setCity(
            city
    );


    destination.setCountry(
            country
    );


    destination.setArrivalDate(
            arrivalDate
    );


    destination.setDepartureDate(
            departureDate
    );


    return Optional.of(
            destinationRepository.save(
                    destination
            )
    );
}


/*
 * Elimina una destinazione
 * da un itinerario Travelly.
 */
@Transactional
public boolean deleteTemplateDestination(
        Long templateId,
        Long destinationId) {


    Optional<Destination> destinationOptional =
            findTemplateDestination(
                    templateId,
                    destinationId
            );


    if (destinationOptional.isEmpty()) {

        return false;
    }


    destinationRepository.delete(
            destinationOptional.get()
    );


    return true;
}





    /*
     * Regola di business sulle date.
     *
     * 1. arrivo e partenza devono esistere
     * 2. arrivo <= partenza
     * 3. la destinazione deve essere compresa
     *    nelle date del viaggio
     */
    private boolean datesAreValid(
            Trip trip,
            LocalDate arrivalDate,
            LocalDate departureDate) {


        if (arrivalDate == null ||
                departureDate == null) {

            return false;
        }


        /*
         * Non posso partire dalla destinazione
         * prima di esserci arrivato.
         */
        if (arrivalDate.isAfter(departureDate)) {
            return false;
        }


        /*
         * La destinazione non può iniziare
         * prima del viaggio.
         */
        if (trip.getStartDate() != null &&
                arrivalDate.isBefore(
                        trip.getStartDate()
                )) {

            return false;
        }


        /*
         * La destinazione non può terminare
         * dopo la fine del viaggio.
         */
        if (trip.getEndDate() != null &&
                departureDate.isAfter(
                        trip.getEndDate()
                )) {

            return false;
        }


        return true;
    }
}