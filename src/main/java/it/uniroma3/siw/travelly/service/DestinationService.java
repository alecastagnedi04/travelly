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

        this.destinationRepository =
                destinationRepository;

        this.tripRepository =
                tripRepository;
    }



    /*
     * =====================================================
     * CREA DESTINAZIONE PERSONALE
     * =====================================================
     *
     * La destinazione viene creata solamente se:
     *
     * - il viaggio appartiene all'utente
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
         * Controlliamo che le date
         * siano coerenti con il viaggio.
         */
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
     * =====================================================
     * RECUPERA DESTINAZIONE PERSONALE
     * =====================================================
     *
     * Metodo di sola lettura.
     */
    @Transactional(readOnly = true)
    public Optional<Destination> findByIdAndOwnerUsername(
            Long tripId,
            Long destinationId,
            String ownerUsername) {


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



        Optional<Destination> destinationOptional =
                destinationRepository
                        .findById(
                                destinationId
                        );


        if (destinationOptional.isEmpty()) {

            return Optional.empty();
        }



        Destination destination =
                destinationOptional.get();



        /*
         * La Destination deve appartenere
         * realmente al Trip indicato.
         */
        if (destination.getTrip() == null ||
                !destination
                        .getTrip()
                        .getId()
                        .equals(
                                tripId
                        )) {

            return Optional.empty();
        }



        return Optional.of(
                destination
        );
    }



    /*
     * =====================================================
     * MODIFICA DESTINAZIONE PERSONALE
     * =====================================================
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
         * Le nuove date devono continuare
         * ad essere valide.
         */
        if (!datesAreValid(
                trip,
                arrivalDate,
                departureDate)) {

            return Optional.empty();
        }



        Optional<Destination> destinationOptional =
                destinationRepository
                        .findById(
                                destinationId
                        );


        if (destinationOptional.isEmpty()) {

            return Optional.empty();
        }



        Destination destination =
                destinationOptional.get();



        /*
         * La Destination deve appartenere
         * proprio al viaggio indicato.
         */
        if (destination.getTrip() == null ||
                !destination
                        .getTrip()
                        .getId()
                        .equals(
                                tripId
                        )) {

            return Optional.empty();
        }



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
     * =====================================================
     * ELIMINA DESTINAZIONE PERSONALE
     * =====================================================
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
                destinationRepository
                        .findById(
                                destinationId
                        );


        if (destinationOptional.isEmpty()) {

            return false;
        }



        Destination destination =
                destinationOptional.get();



        if (destination.getTrip() == null ||
                !destination
                        .getTrip()
                        .getId()
                        .equals(
                                tripId
                        )) {

            return false;
        }



        destinationRepository.delete(
                destination
        );


        return true;
    }



    /*
     * =====================================================
     * DESTINAZIONI DEI TEMPLATE TRAVELLY
     * =====================================================
     */



    /*
     * Crea una destinazione all'interno
     * di una guida ufficiale Travelly.
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
     * Recupera una destinazione del template.
     *
     * Metodo di sola lettura.
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
                destinationRepository
                        .findById(
                                destinationId
                        );


        if (destinationOptional.isEmpty()) {

            return Optional.empty();
        }



        Destination destination =
                destinationOptional.get();



        if (destination.getTrip() == null ||
                !destination
                        .getTrip()
                        .getId()
                        .equals(
                                templateId
                        )) {

            return Optional.empty();
        }



        return Optional.of(
                destination
        );
    }



    /*
     * Modifica destinazione template.
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
     * Elimina destinazione template.
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
     * =====================================================
     * VALIDAZIONE DATE
     * =====================================================
     *
     * Regole:
     *
     * 1. arrivo e partenza devono esistere
     * 2. arrivo <= partenza
     * 3. destinazione dentro le date del viaggio
     */
    private boolean datesAreValid(
            Trip trip,
            LocalDate arrivalDate,
            LocalDate departureDate) {


        if (trip == null ||
                arrivalDate == null ||
                departureDate == null) {

            return false;
        }



        /*
         * Non posso lasciare una destinazione
         * prima di esserci arrivato.
         */
        if (arrivalDate.isAfter(
                departureDate
        )) {

            return false;
        }



        /*
         * Non può iniziare prima del Trip.
         */
        if (trip.getStartDate() != null &&
                arrivalDate.isBefore(
                        trip.getStartDate()
                )) {

            return false;
        }



        /*
         * Non può terminare dopo il Trip.
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