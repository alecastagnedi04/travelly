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
     * Crea un'attività solamente se il Trip
     * appartiene all'utente autenticato.
     *
     * Se il costo è maggiore di zero,
     * genera automaticamente anche una Expense.
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


                    /*
                     * 1. Creazione Activity.
                     */
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
                     * 2. Se l'attività ha un costo,
                     * generiamo automaticamente una Expense.
                     */
                    if (cost > 0) {

                        Expense expense = new Expense();

                        expense.setDescription(name);
                        expense.setAmount(cost);
                        expense.setDate(date);


                        /*
                         * La categoria economica viene ricavata
                         * dalla categoria dell'Activity.
                         */
                        expense.setCategory(
                                convertActivityCategory(category)
                        );


                        expense.setTrip(trip);

                        /*
                         * Il pagatore verrà assegnato dopo.
                         */
                        expense.setPaidBy(null);


                        /*
                         * Colleghiamo la Expense alla Activity
                         * che l'ha generata.
                         */
                        expense.setActivity(savedActivity);


                        expenseRepository.save(expense);
                    }


                    return savedActivity;
                });
    }


    /*
     * Converte la categoria di una Activity
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
     * Elimina un'Activity solamente se appartiene
     * a un Trip dell'utente autenticato.
     *
     * Se l'Activity aveva generato una Expense,
     * eliminiamo anche quella Expense.
     */
    @Transactional
    public boolean deleteActivity(
            Long tripId,
            Long activityId,
            String ownerUsername) {


        /*
         * Controllo 1:
         * il Trip deve appartenere all'utente.
         */
        Optional<Trip> tripOptional =
                tripRepository.findByIdAndOwnerUsername(
                        tripId,
                        ownerUsername
                );


        if (tripOptional.isEmpty()) {
            return false;
        }


        /*
         * Controllo 2:
         * l'Activity deve esistere.
         */
        Optional<Activity> activityOptional =
                activityRepository.findById(activityId);


        if (activityOptional.isEmpty()) {
            return false;
        }


        Activity activity =
                activityOptional.get();


        /*
         * Controllo 3:
         * l'Activity deve appartenere proprio
         * al Trip indicato.
         */
        if (activity.getTrip() == null ||
                !activity.getTrip()
                        .getId()
                        .equals(tripId)) {

            return false;
        }


        /*
         * Se esiste una Expense generata da
         * questa Activity, la eliminiamo prima.
         */
        expenseRepository
                .findByActivityId(activityId)
                .ifPresent(expenseRepository::delete);


        /*
         * Infine eliminiamo l'Activity.
         */
        activityRepository.delete(activity);


        return true;
    }
}