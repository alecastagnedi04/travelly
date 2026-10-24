package it.uniroma3.siw.travelly.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.travelly.model.Activity;
import it.uniroma3.siw.travelly.model.ActivityCategory;
import it.uniroma3.siw.travelly.model.Expense;
import it.uniroma3.siw.travelly.model.ExpenseCategory;
import it.uniroma3.siw.travelly.model.Trip;
import it.uniroma3.siw.travelly.repository.ActivityRepository;
import it.uniroma3.siw.travelly.repository.ExpenseRepository;
import it.uniroma3.siw.travelly.repository.TripRepository;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final ExpenseRepository expenseRepository;
    private final TripRepository tripRepository;


    public ActivityService(
            ActivityRepository activityRepository,
            ExpenseRepository expenseRepository,
            TripRepository tripRepository) {

        this.activityRepository = activityRepository;
        this.expenseRepository = expenseRepository;
        this.tripRepository = tripRepository;
    }


    /*
     * Crea un'attività solamente se il viaggio
     * appartiene all'utente autenticato.
     *
     * Se il costo è maggiore di 0,
     * viene creata automaticamente anche una Expense.
     */
    @Transactional
    public Optional<Activity> createActivity(
            Long tripId,
            String name,
            String description,
            LocalDate date,
            LocalTime time,
            String location,
            ActivityCategory category,
            Double cost,
            String ownerUsername) {


        /*
         * Il costo non può essere negativo.
         */
        if (cost == null || cost < 0) {
            return Optional.empty();
        }


        return tripRepository
                .findByIdAndOwnerUsername(
                        tripId,
                        ownerUsername
                )
                .map(trip -> {

                    Activity activity = new Activity();

                    activity.setName(name);
                    activity.setDescription(description);
                    activity.setDate(date);
                    activity.setTime(time);
                    activity.setLocation(location);
                    activity.setCategory(category);
                    activity.setCost(cost);
                    activity.setTrip(trip);


                    Activity savedActivity =
                            activityRepository.save(activity);


                    /*
                     * Se l'attività ha un costo,
                     * creiamo automaticamente una Expense.
                     */
                    if (cost > 0) {

                        Expense expense = new Expense();

                        expense.setDescription(name);
                        expense.setAmount(cost);
                        expense.setDate(date);

                        expense.setCategory(
                                convertActivityCategory(category)
                        );

                        expense.setTrip(trip);

                        /*
                         * Appena creata la spesa non ha
                         * ancora un partecipante assegnato.
                         */
                        expense.setPaidBy(null);

                        /*
                         * Colleghiamo la spesa all'attività.
                         */
                        expense.setActivity(savedActivity);

                        expenseRepository.save(expense);
                    }


                    return savedActivity;
                });
    }


    /*
     * Modifica un'attività e mantiene sincronizzata
     * l'eventuale Expense collegata.
     */
    @Transactional
    public Optional<Activity> updateActivity(
            Long tripId,
            Long activityId,
            String name,
            String description,
            LocalDate date,
            LocalTime time,
            String location,
            ActivityCategory category,
            Double cost,
            String ownerUsername) {


        /*
         * Il costo non può essere negativo.
         */
        if (cost == null || cost < 0) {
            return Optional.empty();
        }


        /*
         * Controlliamo che il viaggio appartenga
         * all'utente autenticato.
         */
        Optional<Trip> tripOptional =
                tripRepository.findByIdAndOwnerUsername(
                        tripId,
                        ownerUsername
                );


        if (tripOptional.isEmpty()) {
            return Optional.empty();
        }


        /*
         * Recuperiamo l'attività.
         */
        Optional<Activity> activityOptional =
                activityRepository.findById(activityId);


        if (activityOptional.isEmpty()) {
            return Optional.empty();
        }


        Activity activity =
                activityOptional.get();


        /*
         * Controlliamo che l'attività appartenga
         * realmente al viaggio indicato.
         */
        if (activity.getTrip() == null ||
                !activity.getTrip()
                        .getId()
                        .equals(tripId)) {

            return Optional.empty();
        }


        /*
         * Aggiorniamo i dati dell'Activity.
         */
        activity.setName(name);
        activity.setDescription(description);
        activity.setDate(date);
        activity.setTime(time);
        activity.setLocation(location);
        activity.setCategory(category);
        activity.setCost(cost);


        Activity savedActivity =
                activityRepository.save(activity);


        /*
         * Cerchiamo la Expense collegata
         * a questa Activity.
         */
        Optional<Expense> expenseOptional =
                expenseRepository.findByActivityId(
                        activityId
                );


        /*
         * CASO 1:
         *
         * Il nuovo costo è maggiore di zero.
         *
         * Deve esistere una Expense.
         */
        if (cost > 0) {


            /*
             * La Expense esiste già.
             *
             * Aggiorniamo solamente:
             *
             * - descrizione
             * - importo
             * - data
             * - categoria
             *
             * NON tocchiamo paidBy.
             */
            if (expenseOptional.isPresent()) {

                Expense expense =
                        expenseOptional.get();


                expense.setDescription(name);
                expense.setAmount(cost);
                expense.setDate(date);

                expense.setCategory(
                        convertActivityCategory(category)
                );


                /*
                 * IMPORTANTE:
                 *
                 * Qui NON facciamo:
                 *
                 * expense.setPaidBy(null);
                 *
                 * quindi se la spesa era stata pagata
                 * da Gabriele, Gabriele rimane.
                 */


                expenseRepository.save(expense);
            }


            /*
             * La Expense NON esiste.
             *
             * Per esempio:
             *
             * prima Activity costava 0 €
             * ora Activity costa 20 €
             *
             * quindi dobbiamo creare una nuova Expense.
             */
            else {

                Expense expense =
                        new Expense();


                expense.setDescription(name);
                expense.setAmount(cost);
                expense.setDate(date);

                expense.setCategory(
                        convertActivityCategory(category)
                );


                expense.setTrip(
                        tripOptional.get()
                );


                /*
                 * Essendo una nuova Expense,
                 * inizialmente non sappiamo chi ha pagato.
                 */
                expense.setPaidBy(null);


                expense.setActivity(
                        savedActivity
                );


                expenseRepository.save(expense);
            }
        }


        /*
         * CASO 2:
         *
         * Il costo è diventato 0.
         *
         * L'Activity rimane,
         * ma la Expense automatica viene eliminata.
         */
        else {

            expenseOptional.ifPresent(
                    expenseRepository::delete
            );
        }


        return Optional.of(savedActivity);
    }



/*
 * =====================================================
 * ATTIVITÀ DEGLI ITINERARI TRAVELLY
 * =====================================================
 *
 * IMPORTANTE:
 *
 * queste Activity NON generano Expense.
 */


/*
 * Crea una Activity dentro
 * un itinerario ufficiale Travelly.
 */
@Transactional
public Optional<Activity> createTemplateActivity(
        Long templateId,
        String name,
        String description,
        LocalDate date,
        LocalTime time,
        String location,
        ActivityCategory category,
        Double cost) {


    if (cost == null || cost < 0) {

        return Optional.empty();
    }


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


    /*
     * L'attività deve essere compresa
     * nelle date dell'itinerario.
     */
    if (!activityDateIsValid(
            trip,
            date)) {

        return Optional.empty();
    }


    Activity activity =
            new Activity();


    activity.setName(
            name
    );


    activity.setDescription(
            description
    );


    activity.setDate(
            date
    );


    activity.setTime(
            time
    );


    activity.setLocation(
            location
    );


    activity.setCategory(
            category
    );


    activity.setCost(
            cost
    );


    activity.setTrip(
            trip
    );


    /*
     * Qui NON creiamo Expense.
     */
    return Optional.of(
            activityRepository.save(
                    activity
            )
    );
}


/*
 * Recupera una Activity verificando
 * che appartenga al template.
 */
@Transactional(readOnly = true)
public Optional<Activity> findTemplateActivity(
        Long templateId,
        Long activityId) {


    if (tripRepository
            .findByIdAndTemplateTrue(
                    templateId
            )
            .isEmpty()) {

        return Optional.empty();
    }


    Optional<Activity> activityOptional =
            activityRepository.findById(
                    activityId
            );


    if (activityOptional.isEmpty()) {

        return Optional.empty();
    }


    Activity activity =
            activityOptional.get();


    if (activity.getTrip() == null ||
            !activity.getTrip()
                    .getId()
                    .equals(templateId)) {

        return Optional.empty();
    }


    return Optional.of(
            activity
    );
}


/*
 * Modifica una Activity del template.
 *
 * Anche in questo caso NON tocchiamo Expense.
 */
@Transactional
public Optional<Activity> updateTemplateActivity(
        Long templateId,
        Long activityId,
        String name,
        String description,
        LocalDate date,
        LocalTime time,
        String location,
        ActivityCategory category,
        Double cost) {


    if (cost == null || cost < 0) {

        return Optional.empty();
    }


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


    if (!activityDateIsValid(
            trip,
            date)) {

        return Optional.empty();
    }


    Optional<Activity> activityOptional =
            findTemplateActivity(
                    templateId,
                    activityId
            );


    if (activityOptional.isEmpty()) {

        return Optional.empty();
    }


    Activity activity =
            activityOptional.get();


    activity.setName(
            name
    );


    activity.setDescription(
            description
    );


    activity.setDate(
            date
    );


    activity.setTime(
            time
    );


    activity.setLocation(
            location
    );


    activity.setCategory(
            category
    );


    activity.setCost(
            cost
    );


    return Optional.of(
            activityRepository.save(
                    activity
            )
    );
}


/*
 * Elimina una Activity del template.
 */
@Transactional
public boolean deleteTemplateActivity(
        Long templateId,
        Long activityId) {


    Optional<Activity> activityOptional =
            findTemplateActivity(
                    templateId,
                    activityId
            );


    if (activityOptional.isEmpty()) {

        return false;
    }


    Activity activity =
            activityOptional.get();


    /*
     * Normalmente un template non possiede Expense.
     *
     * Questo controllo ci protegge comunque
     * da eventuali dati incoerenti.
     */
    expenseRepository
            .findByActivityId(
                    activityId
            )
            .ifPresent(
                    expenseRepository::delete
            );


    activityRepository.delete(
            activity
    );


    return true;
}


/*
 * Controlla che la data dell'attività
 * rientri nelle date del template.
 */
private boolean activityDateIsValid(
        Trip trip,
        LocalDate date) {


    if (date == null) {

        return false;
    }


    if (trip.getStartDate() != null &&
            date.isBefore(
                    trip.getStartDate()
            )) {

        return false;
    }


    if (trip.getEndDate() != null &&
            date.isAfter(
                    trip.getEndDate()
            )) {

        return false;
    }


    return true;
}




    /*
     * Converte la categoria dell'Activity
     * nella corrispondente categoria Expense.
     */
    private ExpenseCategory convertActivityCategory(
            ActivityCategory category) {

        return switch (category) {

            case FOOD ->
                    ExpenseCategory.FOOD;

            case CULTURE ->
                    ExpenseCategory.CULTURE;

            case TRANSPORT ->
                    ExpenseCategory.TRANSPORT;

            case ENTERTAINMENT ->
                    ExpenseCategory.ENTERTAINMENT;

            case OTHER ->
                    ExpenseCategory.OTHER;
        };
    }


    /*
     * Elimina un'Activity solamente se:
     *
     * 1. il viaggio appartiene all'utente autenticato
     * 2. l'Activity esiste
     * 3. l'Activity appartiene realmente a quel viaggio
     *
     * Se esiste una Expense automatica collegata,
     * viene eliminata prima dell'Activity.
     */
    @Transactional
    public boolean deleteActivity(
            Long tripId,
            Long activityId,
            String ownerUsername) {


        Optional<Trip> tripOptional =
                tripRepository.findByIdAndOwnerUsername(
                        tripId,
                        ownerUsername
                );


        if (tripOptional.isEmpty()) {
            return false;
        }


        Optional<Activity> activityOptional =
                activityRepository.findById(
                        activityId
                );


        if (activityOptional.isEmpty()) {
            return false;
        }


        Activity activity =
                activityOptional.get();


        /*
         * Controllo di appartenenza.
         */
        if (activity.getTrip() == null ||
                !activity.getTrip()
                        .getId()
                        .equals(tripId)) {

            return false;
        }


        /*
         * Eliminiamo prima l'eventuale
         * Expense collegata.
         */
        expenseRepository
                .findByActivityId(activityId)
                .ifPresent(
                        expenseRepository::delete
                );


        /*
         * Poi eliminiamo l'Activity.
         */
        activityRepository.delete(activity);


        return true;
    }
}