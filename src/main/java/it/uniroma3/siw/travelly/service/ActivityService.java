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



    /*
     * Recuperiamo il viaggio verificando
     * contemporaneamente che appartenga
     * all'utente autenticato.
     */
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
     * La data dell'attività deve essere
     * compresa tra la partenza e il ritorno
     * del viaggio.
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



    Activity savedActivity =
            activityRepository.save(
                    activity
            );



    /*
     * Se l'attività costa più di 0 €,
     * viene creata automaticamente
     * anche una Expense collegata.
     */
    if (cost > 0) {


        Expense expense =
                new Expense();


        expense.setDescription(
                name
        );


        expense.setAmount(
                cost
        );


        expense.setDate(
                date
        );


        expense.setCategory(
                convertActivityCategory(
                        category
                )
        );


        expense.setTrip(
                trip
        );


        /*
         * Il pagatore verrà scelto
         * successivamente.
         */
        expense.setPaidBy(
                null
        );


        /*
         * Colleghiamo Activity ed Expense.
         */
        expense.setActivity(
                savedActivity
        );


        expenseRepository.save(
                expense
        );
    }



    return Optional.of(
            savedActivity
    );
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
     * Il viaggio deve appartenere
     * all'utente autenticato.
     */
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
     * Anche durante una modifica
     * l'attività deve restare all'interno
     * delle date del viaggio.
     */
    if (!activityDateIsValid(
            trip,
            date)) {

        return Optional.empty();
    }



    /*
     * Recuperiamo l'attività.
     */
    Optional<Activity> activityOptional =
            activityRepository
                    .findById(
                            activityId
                    );


    if (activityOptional.isEmpty()) {

        return Optional.empty();
    }



    Activity activity =
            activityOptional.get();



    /*
     * Controlliamo che l'Activity appartenga
     * realmente al Trip indicato nell'URL.
     */
    if (activity.getTrip() == null ||
            !activity.getTrip()
                    .getId()
                    .equals(
                            tripId
                    )) {

        return Optional.empty();
    }



    /*
     * =====================================================
     * AGGIORNAMENTO ACTIVITY
     * =====================================================
     */

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



    Activity savedActivity =
            activityRepository.save(
                    activity
            );



    /*
     * Cerchiamo l'eventuale Expense
     * già collegata all'Activity.
     */
    Optional<Expense> expenseOptional =
            expenseRepository
                    .findByActivityId(
                            activityId
                    );



    /*
     * =====================================================
     * COSTO > 0
     * =====================================================
     *
     * Deve esistere una Expense.
     */
    if (cost > 0) {


        /*
         * Expense già presente:
         * la sincronizziamo con l'Activity.
         */
        if (expenseOptional.isPresent()) {


            Expense expense =
                    expenseOptional.get();


            expense.setDescription(
                    name
            );


            expense.setAmount(
                    cost
            );


            expense.setDate(
                    date
            );


            expense.setCategory(
                    convertActivityCategory(
                            category
                    )
            );


            /*
             * NON tocchiamo paidBy:
             * se era già stato scelto
             * un pagatore, rimane.
             */
            expenseRepository.save(
                    expense
            );
        }


        /*
         * Non esiste ancora una Expense.
         *
         * Esempio:
         *
         * Activity prima = 0 €
         * Activity ora   = 20 €
         */
        else {


            Expense expense =
                    new Expense();


            expense.setDescription(
                    name
            );


            expense.setAmount(
                    cost
            );


            expense.setDate(
                    date
            );


            expense.setCategory(
                    convertActivityCategory(
                            category
                    )
            );


            expense.setTrip(
                    trip
            );


            expense.setPaidBy(
                    null
            );


            expense.setActivity(
                    savedActivity
            );


            expenseRepository.save(
                    expense
            );
        }
    }



    /*
     * =====================================================
     * COSTO = 0
     * =====================================================
     *
     * L'Activity rimane,
     * l'eventuale Expense automatica sparisce.
     */
    else {


        expenseOptional.ifPresent(
                expenseRepository::delete
        );
    }



    return Optional.of(
            savedActivity
    );
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
 * =====================================================
 * VALIDAZIONE DATA ATTIVITÀ
 * =====================================================
 *
 * Una Activity deve cadere all'interno
 * dell'intervallo temporale del Trip.
 *
 * Vale sia per:
 *
 * - viaggi personali
 * - itinerari ufficiali Travelly
 */
private boolean activityDateIsValid(
        Trip trip,
        LocalDate date) {


    if (trip == null ||
            date == null) {

        return false;
    }



    /*
     * Non può avvenire prima
     * dell'inizio del viaggio.
     */
    if (trip.getStartDate() != null &&
            date.isBefore(
                    trip.getStartDate()
            )) {

        return false;
    }



    /*
     * Non può avvenire dopo
     * la fine del viaggio.
     */
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