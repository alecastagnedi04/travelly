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

        this.tripRepository =
                tripRepository;

        this.userRepository =
                userRepository;
    }



    /*
     * =====================================================
     * SALVATAGGIO GENERICO
     * =====================================================
     */
    @Transactional
    public Trip save(
            Trip trip) {

        return tripRepository.save(
                trip
        );
    }



    /*
     * =====================================================
     * TUTTI I VIAGGI
     * =====================================================
     *
     * Utile soprattutto per l'ADMIN.
     */
    @Transactional(readOnly = true)
    public Iterable<Trip> findAll() {

        return tripRepository.findAll();
    }



    /*
     * =====================================================
     * CERCA TRIP PER ID
     * =====================================================
     *
     * Utilizzato soprattutto
     * dalle funzionalità amministrative.
     */
    @Transactional(readOnly = true)
    public Optional<Trip> findById(
            Long id) {

        return tripRepository.findById(
                id
        );
    }



    /*
     * =====================================================
     * VIAGGI PERSONALI DELL'UTENTE
     * =====================================================
     *
     * Restituisce solamente:
     *
     * owner.username = username
     * template = false
     *
     * Quindi gli itinerari ufficiali Travelly
     * non compariranno in "I miei viaggi".
     */
    @Transactional(readOnly = true)
    public Iterable<Trip> findByOwnerUsername(
            String username) {

        return tripRepository
                .findByOwnerUsernameAndTemplateFalse(
                        username
                );
    }



    /*
     * =====================================================
     * CERCA VIAGGIO DEL PROPRIETARIO
     * =====================================================
     *
     * Verifica contemporaneamente:
     *
     * - id viaggio
     * - username proprietario
     *
     * Serve per la sicurezza.
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
     * =====================================================
     * ITINERARI UFFICIALI TRAVELLY
     * =====================================================
     *
     * Restituisce tutti i Trip
     * con template = true.
     *
     * Verrà utilizzato:
     *
     * - dalla homepage
     * - da /explore
     * - dalla dashboard ADMIN
     */
    @Transactional(readOnly = true)
    public Iterable<Trip> findTemplates() {

        return tripRepository
                .findByTemplateTrue();
    }



    /*
     * Cerca uno specifico itinerario ufficiale.
     */
    @Transactional(readOnly = true)
    public Optional<Trip> findTemplateById(
            Long id) {

        return tripRepository
                .findByIdAndTemplateTrue(
                        id
                );
    }



    /*
     * =====================================================
     * CREA VIAGGIO PERSONALE
     * =====================================================
     *
     * Crea un normale viaggio appartenente
     * all'utente autenticato.
     */
    @Transactional
    public Trip createTrip(
            String name,
            String description,
            LocalDate startDate,
            LocalDate endDate,
            Double budget,
            String coverImage,
            String ownerUsername) {


        User owner =
                userRepository
                        .findByUsername(
                                ownerUsername
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Utente autenticato non trovato"
                                )
                        );


        Trip trip =
                new Trip();


        trip.setName(
                name
        );


        trip.setDescription(
                description
        );


        trip.setStartDate(
                startDate
        );


        trip.setEndDate(
                endDate
        );


        trip.setBudget(
                budget
        );


        trip.setCoverImage(
                coverImage
        );


        /*
         * È un normale viaggio personale.
         */
        trip.setTemplate(
                false
        );


        /*
         * Proprietario del viaggio.
         */
        trip.setOwner(
                owner
        );


        return tripRepository.save(
                trip
        );
    }



    /*
     * =====================================================
     * MODIFICA VIAGGIO PERSONALE
     * =====================================================
     *
     * Un utente può modificare solamente
     * un proprio Trip.
     */
    @Transactional
    public Optional<Trip> updateTrip(
            Long id,
            String name,
            String description,
            LocalDate startDate,
            LocalDate endDate,
            Double budget,
            String coverImage,
            String ownerUsername) {


        return tripRepository
                .findByIdAndOwnerUsername(
                        id,
                        ownerUsername
                )
                .map(trip -> {


                    trip.setName(
                            name
                    );


                    trip.setDescription(
                            description
                    );


                    trip.setStartDate(
                            startDate
                    );


                    trip.setEndDate(
                            endDate
                    );


                    trip.setBudget(
                            budget
                    );


                    trip.setCoverImage(
                            coverImage
                    );


                    return tripRepository.save(
                            trip
                    );
                });
    }



    /*
     * =====================================================
     * ELIMINAZIONE GENERICA
     * =====================================================
     *
     * Utilizzata dall'ADMIN.
     */
    @Transactional
    public void deleteById(
            Long id) {

        tripRepository.deleteById(
                id
        );
    }



    /*
     * =====================================================
     * ELIMINAZIONE SICURA PER USER
     * =====================================================
     *
     * Elimina il viaggio solamente se
     * appartiene all'utente autenticato.
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
     * =====================================================
     * TOTALE SPESE
     * =====================================================
     */
    public Double calculateTotalExpenses(
            Trip trip) {


        double total =
                0.0;


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
     * =====================================================
     * BUDGET RIMANENTE
     * =====================================================
     */
    public Double calculateRemainingBudget(
            Trip trip) {


        double budget =
                0.0;


        if (trip.getBudget() != null) {

            budget =
                    trip.getBudget();
        }


        return budget -
                calculateTotalExpenses(
                        trip
                );
    }

    /*
 * =====================================================
 * CREA ITINERARIO UFFICIALE TRAVELLY
 * =====================================================
 *
 * Gli itinerari ufficiali:
 *
 * - template = true
 * - owner = null
 *
 * Possono essere creati solamente
 * attraverso l'area ADMIN.
 */
@Transactional
public Trip createTemplate(
        String name,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        Double budget,
        String coverImage) {


    Trip trip =
            new Trip();


    trip.setName(
            name
    );


    trip.setDescription(
            description
    );


    trip.setStartDate(
            startDate
    );


    trip.setEndDate(
            endDate
    );


    trip.setBudget(
            budget
    );


    trip.setCoverImage(
            coverImage
    );


    /*
     * È un itinerario ufficiale.
     */
    trip.setTemplate(
            true
    );


    /*
     * Non appartiene ad un normale utente.
     */
    trip.setOwner(
            null
    );


    return tripRepository.save(
            trip
    );
}


/*
 * =====================================================
 * MODIFICA ITINERARIO UFFICIALE
 * =====================================================
 */
@Transactional
public Optional<Trip> updateTemplate(
        Long id,
        String name,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        Double budget,
        String coverImage) {


    return tripRepository
            .findByIdAndTemplateTrue(
                    id
            )
            .map(trip -> {


                trip.setName(
                        name
                );


                trip.setDescription(
                        description
                );


                trip.setStartDate(
                        startDate
                );


                trip.setEndDate(
                        endDate
                );


                trip.setBudget(
                        budget
                );


                trip.setCoverImage(
                        coverImage
                );


                return tripRepository.save(
                        trip
                );
            });
}


/*
 * =====================================================
 * ELIMINA ITINERARIO UFFICIALE
 * =====================================================
 */
@Transactional
public boolean deleteTemplate(
        Long id) {


    Optional<Trip> trip =
            tripRepository
                    .findByIdAndTemplateTrue(
                            id
                    );


    if (trip.isEmpty()) {

        return false;
    }


    tripRepository.delete(
            trip.get()
    );


    return true;
}
}


