package it.uniroma3.siw.travelly.service;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.stereotype.Service;

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
     * Questo metodo rimane utile anche per future operazioni.
     */
    public Trip save(Trip trip) {

        return tripRepository.save(trip);
    }


    /*
     * Restituisce tutti i Trip presenti nel database.
     *
     * NON lo useremo più per la pagina "I miei viaggi",
     * ma lo teniamo perché sarà utile soprattutto all'ADMIN.
     */
    public Iterable<Trip> findAll() {

        return tripRepository.findAll();
    }


    /*
     * Cerca un Trip solamente attraverso il suo id.
     *
     * Anche questo rimane utile per le operazioni amministrative.
     */
    public Optional<Trip> findById(Long id) {

        return tripRepository.findById(id);
    }


    /*
     * Restituisce solamente i Trip appartenenti
     * all'utente con lo username specificato.
     */
    public Iterable<Trip> findByOwnerUsername(String username) {

        return tripRepository.findByOwnerUsername(username);
    }


    /*
     * Cerca un Trip verificando contemporaneamente:
     *
     * 1. l'id del Trip
     * 2. lo username del proprietario
     *
     * Serve per evitare che un utente possa accedere
     * al viaggio di un altro utente modificando l'URL.
     */
    public Optional<Trip> findByIdAndOwnerUsername(
            Long id,
            String username) {

        return tripRepository.findByIdAndOwnerUsername(
                id,
                username
        );
    }


    /*
     * Crea un nuovo viaggio e lo associa
     * all'utente autenticato.
     */
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
         * Questa è la riga fondamentale:
         * il nuovo Trip appartiene all'utente autenticato.
         */
        trip.setOwner(owner);

        return tripRepository.save(trip);
    }


    /*
     * Modifica un viaggio solamente se appartiene
     * all'utente autenticato.
     */
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
     * La manteniamo perché servirà all'ADMIN,
     * che dovrà poter eliminare qualsiasi Trip.
     */
    public void deleteById(Long id) {

        tripRepository.deleteById(id);
    }


    /*
     * Eliminazione sicura per un normale USER.
     *
     * Cancella il viaggio soltanto se l'utente
     * autenticato ne è realmente il proprietario.
     */
    public boolean deleteByIdForOwner(
            Long id,
            String ownerUsername) {

        Optional<Trip> trip =
                tripRepository.findByIdAndOwnerUsername(
                        id,
                        ownerUsername
                );

        if (trip.isEmpty()) {
            return false;
        }

        tripRepository.delete(trip.get());

        return true;
    }


    /*
     * Calcola la somma di tutte le spese
     * associate al viaggio.
     */
    public Double calculateTotalExpenses(Trip trip) {

        double total = 0.0;

        for (Expense expense : trip.getExpenses()) {

            if (expense.getAmount() != null) {
                total += expense.getAmount();
            }
        }

        return total;
    }


    /*
     * Calcola il budget ancora disponibile:
     *
     * budget iniziale - spese totali
     */
    public Double calculateRemainingBudget(Trip trip) {

        double budget = 0.0;

        if (trip.getBudget() != null) {
            budget = trip.getBudget();
        }

        return budget - calculateTotalExpenses(trip);
    }
}