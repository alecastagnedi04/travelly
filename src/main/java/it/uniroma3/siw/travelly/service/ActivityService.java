package it.uniroma3.siw.travelly.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import it.uniroma3.siw.travelly.model.Activity;
import it.uniroma3.siw.travelly.model.ActivityCategory;
import it.uniroma3.siw.travelly.repository.ActivityRepository;
import it.uniroma3.siw.travelly.repository.TripRepository;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final TripRepository tripRepository;

    public ActivityService(ActivityRepository activityRepository,
                           TripRepository tripRepository) {

        this.activityRepository = activityRepository;
        this.tripRepository = tripRepository;
    }

    public Optional<Activity> createActivity(
            Long tripId,
            String name,
            String description,
            LocalDate date,
            LocalTime time,
            String location,
            ActivityCategory category,
            Double cost) {

        return tripRepository.findById(tripId)
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

                    return activityRepository.save(activity);
                });
    }
}