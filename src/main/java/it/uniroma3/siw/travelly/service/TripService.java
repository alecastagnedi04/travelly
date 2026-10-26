package it.uniroma3.siw.travelly.service;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.travelly.model.Activity;
import it.uniroma3.siw.travelly.model.Destination;
import it.uniroma3.siw.travelly.model.Trip;
import it.uniroma3.siw.travelly.model.TripVisibility;
import it.uniroma3.siw.travelly.model.User;

import it.uniroma3.siw.travelly.repository.ExpenseRepository;
import it.uniroma3.siw.travelly.repository.TripRepository;
import it.uniroma3.siw.travelly.repository.UserRepository;


@Service
public class TripService {


    private final TripRepository tripRepository;

    private final UserRepository userRepository;

    private final ExpenseRepository expenseRepository;

    private final ImageStorageService imageStorageService;



    public TripService(
            TripRepository tripRepository,
            UserRepository userRepository,
            ExpenseRepository expenseRepository,
            ImageStorageService imageStorageService) {


        this.tripRepository =
                tripRepository;


        this.userRepository =
                userRepository;


        this.expenseRepository =
                expenseRepository;


        this.imageStorageService =
                imageStorageService;
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
     */
    @Transactional(readOnly = true)
    public Iterable<Trip> findAll() {


        return tripRepository.findAll();
    }



    /*
     * =====================================================
     * CERCA VIAGGIO PER ID
     * =====================================================
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
     * Esclude i template ufficiali Travelly.
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
     * VIAGGIO PERSONALE + PROPRIETARIO
     * =====================================================
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
     * TEMPLATE UFFICIALI TRAVELLY
     * =====================================================
     */
    @Transactional(readOnly = true)
    public Iterable<Trip> findTemplates() {


        return tripRepository
                .findByTemplateTrue();
    }



    /*
     * Recupera un singolo template.
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
         * È un viaggio personale.
         */
        trip.setTemplate(
                false
        );



        /*
         * Un viaggio personale nasce privato.
         */
        trip.setVisibility(
                TripVisibility.PRIVATE
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
            TripVisibility visibility,
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


                    trip.setVisibility(
                            visibility
                    );



                    /*
                     * Continua ad essere
                     * un viaggio personale.
                     */
                    trip.setTemplate(
                            false
                    );



                    return tripRepository.save(
                            trip
                    );
                });
    }



    /*
     * =====================================================
     * CREA TEMPLATE UFFICIALE
     * =====================================================
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
         * Template ufficiale Travelly.
         */
        trip.setTemplate(
                true
        );



        /*
         * I template ufficiali sono pubblici.
         */
        trip.setVisibility(
                TripVisibility.PUBLIC
        );



        /*
         * Un template non appartiene
         * ad un normale User.
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
     * MODIFICA TEMPLATE UFFICIALE
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



                    /*
                     * Un template deve rimanere
                     * sempre template.
                     */
                    trip.setTemplate(
                            true
                    );



                    /*
                     * E deve rimanere pubblico.
                     */
                    trip.setVisibility(
                            TripVisibility.PUBLIC
                    );



                    trip.setOwner(
                            null
                    );



                    return tripRepository.save(
                            trip
                    );
                });
    }



    /*
     * =====================================================
     * ELIMINA TEMPLATE
     * =====================================================
     */
    @Transactional
    public boolean deleteTemplate(
            Long id) {


        Optional<Trip> tripOptional =
                tripRepository
                        .findByIdAndTemplateTrue(
                                id
                        );


        if (tripOptional.isEmpty()) {


            return false;
        }



        tripRepository.delete(
                tripOptional.get()
        );


        return true;
    }



    /*
     * =====================================================
     * USA QUESTO ITINERARIO
     * =====================================================
     *
     * Crea una copia personale indipendente
     * di un template ufficiale.
     *
     * Copia:
     *
     * - Trip
     * - Destination
     * - Activity
     * - copertina
     *
     * NON copia:
     *
     * - Participant
     * - Expense
     * - payer
     */
    @Transactional
    public Optional<Trip> cloneTemplateForUser(
            Long templateId,
            String ownerUsername) {


        Optional<Trip> templateOptional =
                tripRepository
                        .findByIdAndTemplateTrue(
                                templateId
                        );


        if (templateOptional.isEmpty()) {


            return Optional.empty();
        }



        Optional<User> ownerOptional =
                userRepository
                        .findByUsername(
                                ownerUsername
                        );


        if (ownerOptional.isEmpty()) {


            return Optional.empty();
        }



        Trip template =
                templateOptional.get();


        User owner =
                ownerOptional.get();



        /*
         * =================================================
         * NUOVO VIAGGIO
         * =================================================
         */

        Trip newTrip =
                new Trip();



        newTrip.setName(
                template.getName()
        );


        newTrip.setDescription(
                template.getDescription()
        );


        newTrip.setStartDate(
                template.getStartDate()
        );


        newTrip.setEndDate(
                template.getEndDate()
        );


        newTrip.setBudget(
                template.getBudget()
        );



        /*
         * Non è più un template.
         */
        newTrip.setTemplate(
                false
        );



        /*
         * La copia personale nasce privata.
         */
        newTrip.setVisibility(
                TripVisibility.PRIVATE
        );



        newTrip.setOwner(
                owner
        );



        /*
         * =================================================
         * COPIA COPERTINA
         * =================================================
         */

        String copiedCoverImage =
                imageStorageService
                        .copyTripCover(
                                template.getCoverImage()
                        );


        newTrip.setCoverImage(
                copiedCoverImage
        );



        /*
         * =================================================
         * COPIA DESTINAZIONI
         * =================================================
         */

        for (Destination originalDestination :
                template.getDestinations()) {


            Destination newDestination =
                    new Destination();



            newDestination.setCity(
                    originalDestination.getCity()
            );


            newDestination.setCountry(
                    originalDestination.getCountry()
            );


            newDestination.setArrivalDate(
                    originalDestination.getArrivalDate()
            );


            newDestination.setDepartureDate(
                    originalDestination.getDepartureDate()
            );


            newDestination.setTrip(
                    newTrip
            );



            newTrip
                    .getDestinations()
                    .add(
                            newDestination
                    );
        }



        /*
         * =================================================
         * COPIA ATTIVITÀ
         * =================================================
         *
         * Non utilizziamo ActivityService
         * perché non vogliamo generare Expense.
         */

        for (Activity originalActivity :
                template.getActivities()) {


            Activity newActivity =
                    new Activity();



            newActivity.setName(
                    originalActivity.getName()
            );


            newActivity.setDescription(
                    originalActivity.getDescription()
            );


            newActivity.setDate(
                    originalActivity.getDate()
            );


            newActivity.setTime(
                    originalActivity.getTime()
            );


            newActivity.setLocation(
                    originalActivity.getLocation()
            );


            newActivity.setCategory(
                    originalActivity.getCategory()
            );


            newActivity.setCost(
                    originalActivity.getCost()
            );


            newActivity.setTrip(
                    newTrip
            );



            newTrip
                    .getActivities()
                    .add(
                            newActivity
                    );
        }



        /*
         * CascadeType.ALL salva anche
         * Destination e Activity.
         */
        Trip savedTrip =
                tripRepository.save(
                        newTrip
                );



        return Optional.of(
                savedTrip
        );
    }



    /*
     * =====================================================
     * ELIMINAZIONE GENERICA
     * =====================================================
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
     * ELIMINA VIAGGIO DEL PROPRIETARIO
     * =====================================================
     */
    @Transactional
    public boolean deleteByIdForOwner(
            Long id,
            String ownerUsername) {


        Optional<Trip> tripOptional =
                tripRepository
                        .findByIdAndOwnerUsername(
                                id,
                                ownerUsername
                        );


        if (tripOptional.isEmpty()) {


            return false;
        }



        tripRepository.delete(
                tripOptional.get()
        );


        return true;
    }



    /*
     * =====================================================
     * VIAGGI PUBBLICI DELLA COMMUNITY
     * =====================================================
     */
    @Transactional(readOnly = true)
    public Iterable<Trip> findPublicCommunityTrips() {


        return tripRepository
                .findByTemplateFalseAndVisibilityOrderByIdDesc(
                        TripVisibility.PUBLIC
                );
    }



    /*
     * Recupera un singolo viaggio personale
     * solamente se pubblico.
     */
    @Transactional(readOnly = true)
    public Optional<Trip> findPublicCommunityTripById(
            Long id) {


        return tripRepository
                .findByIdAndTemplateFalseAndVisibility(
                        id,
                        TripVisibility.PUBLIC
                );
    }



    /*
     * =====================================================
     * TOTALE SPESE
     * =====================================================
     *
     * PRIMA:
     *
     * caricavamo tutte le Expense del Trip
     * e le sommavamo con un ciclo Java.
     *
     * ORA:
     *
     * chiediamo direttamente al database
     * di eseguire SUM(e.amount).
     *
     * La query JPQL si trova in
     * ExpenseRepository.
     */
    @Transactional(readOnly = true)
    public Double calculateTotalExpenses(
            Trip trip) {


        if (trip == null ||
                trip.getId() == null) {


            return 0.0;
        }



        Double total =
                expenseRepository
                        .sumAmountByTripId(
                                trip.getId()
                        );



        /*
         * La query utilizza già COALESCE,
         * ma manteniamo comunque questo controllo
         * per robustezza.
         */
        if (total == null) {


            return 0.0;
        }



        return total;
    }



    /*
     * =====================================================
     * BUDGET RIMANENTE
     * =====================================================
     */
    @Transactional(readOnly = true)
    public Double calculateRemainingBudget(
            Trip trip) {


        if (trip == null) {


            return 0.0;
        }



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
     * RICERCA TEMPLATE PER NOME ESATTO
     * =====================================================
     */
    @Transactional(readOnly = true)
    public Iterable<Trip> findTemplatesByExactName(
            String name) {

        return tripRepository
                .findByTemplateTrueAndNameIgnoreCase(
                        name
                );
    }



    /*
     * =====================================================
     * RICERCA VIAGGI PUBBLICI PER NOME ESATTO
     * =====================================================
     *
     * Esclude i template e i viaggi privati.
     */
    @Transactional(readOnly = true)
    public Iterable<Trip> findPublicCommunityTripsByExactName(
            String name) {

        return tripRepository
                .findByTemplateFalseAndVisibilityAndNameIgnoreCaseOrderByIdDesc(
                        TripVisibility.PUBLIC,
                        name
                );
    }
}