package it.uniroma3.siw.travelly.service;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.travelly.model.Expense;
import it.uniroma3.siw.travelly.model.Trip;
import it.uniroma3.siw.travelly.model.User;
import it.uniroma3.siw.travelly.repository.TripRepository;
import it.uniroma3.siw.travelly.repository.UserRepository;


@Service
public class TripService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;


    public TripService(
            TripRepository tripRepository,
            UserRepository userRepository) {

        this.tripRepository = tripRepository;
        this.userRepository = userRepository;
    }


    /*
     * Salva un Trip.
     */
    @Transactional
    public Trip save(Trip trip) {

        return tripRepository.save(trip);
    }


    /*
     * Restituisce tutti i Trip presenti nel database.
     *
     * Utile soprattutto per l'ADMIN.
     */
    @Transactional(readOnly = true)
    public Iterable<Trip> findAll() {

        return tripRepository.findAll();
    }


    /*
     * Cerca un Trip tramite id.
     *
     * Utile per le operazioni amministrative.
     */
    @Transactional(readOnly = true)
    public Optional<Trip> findById(Long id) {

        return tripRepository.findById(id);
    }


    /*
     * Restituisce solamente i Trip appartenenti
     * all'utente con lo username specificato.
     */
    @Transactional(readOnly = true)
    public Iterable<Trip> findByOwnerUsername(
            String username) {

        return tripRepository
                .findByOwnerUsername(username);
    }


    /*
     * Cerca un Trip verificando contemporaneamente:
     *
     * 1. id del Trip
     * 2. username del proprietario
     *
     * Impedisce ad un utente di accedere
     * ai viaggi di altri utenti modificando l'URL.
     */
    @Transactional(readOnly = true)
    public Optional<Trip> findByIdAndOwnerUsername(
            Long id,
            String username) {

        return tripRepository
                .findByIdAndOwnerUsername(
                        id,
                        username
                );
    }


    /*
     * Crea un nuovo viaggio e lo associa
     * all'utente autenticato.
     */
    @Transactional
    public Trip createTrip(
            String name,
            String description,
            LocalDate startDate,
            LocalDate endDate,
            Double budget,
            String ownerUsername) {

        User owner = userRepository
                .findByUsername(ownerUsername)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Utente autenticato non trovato"
                        )
                );


        Trip trip = new Trip();

        trip.setName(name);
        trip.setDescription(description);
        trip.setStartDate(startDate);
        trip.setEndDate(endDate);
        trip.setBudget(budget);

        /*
         * Il nuovo viaggio appartiene
         * all'utente autenticato.
         */
        trip.setOwner(owner);


        return tripRepository.save(trip);
    }


    /*
     * Modifica un viaggio solamente se appartiene
     * all'utente autenticato.
     */
    @Transactional
    public Optional<Trip> updateTrip(
            Long id,
            String name,
            String description,
            LocalDate startDate,
            LocalDate endDate,
            Double budget,
            String ownerUsername) {

        return tripRepository
                .findByIdAndOwnerUsername(
                        id,
                        ownerUsername
                )
                .map(trip -> {

                    trip.setName(name);
                    trip.setDescription(description);
                    trip.setStartDate(startDate);
                    trip.setEndDate(endDate);
                    trip.setBudget(budget);

                    return tripRepository.save(trip);
                });
    }


    /*
     * Eliminazione generica tramite id.
     *
     * Utilizzata dall'ADMIN.
     */
    @Transactional
    public void deleteById(Long id) {

        tripRepository.deleteById(id);
    }


    /*
     * Eliminazione sicura per un normale USER.
     *
     * Il viaggio viene cancellato solamente
     * se appartiene all'utente autenticato.
     */
    @Transactional
    public boolean deleteByIdForOwner(
            Long id,
            String ownerUsername) {

        Optional<Trip> trip =
                tripRepository
                        .findByIdAndOwnerUsername(
                                id,
                                ownerUsername
                        );


        if (trip.isEmpty()) {

            return false;
        }


        tripRepository.delete(
                trip.get()
        );


        return true;
    }


    /*
     * Calcola la somma di tutte le spese
     * associate al viaggio.
     */
    public Double calculateTotalExpenses(
            Trip trip) {

        double total = 0.0;


        for (Expense expense :
                trip.getExpenses()) {

            if (expense.getAmount() != null) {

                total +=
                        expense.getAmount();
            }
        }


        return total;
    }


    /*
     * Calcola:
     *
     * budget iniziale - spese totali
     */
    public Double calculateRemainingBudget(
            Trip trip) {

        double budget = 0.0;


        if (trip.getBudget() != null) {

            budget =
                    trip.getBudget();
        }


        return budget -
                calculateTotalExpenses(trip);
    }
}