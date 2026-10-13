package it.uniroma3.siw.travelly.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import it.uniroma3.siw.travelly.model.Trip;
import it.uniroma3.siw.travelly.repository.TripRepository;
import java.time.LocalDate;
import it.uniroma3.siw.travelly.model.Expense;

@Service
public class TripService {

    private final TripRepository tripRepository;  //tripservice ha bisogno di triprepository per accedere ai trip persistenti; quindi tripservice dipende da triprepository.

    public TripService(TripRepository tripRepository) {
        this.tripRepository = tripRepository;
    }

    public Trip save(Trip trip) {
        return tripRepository.save(trip);
    }

    public Iterable<Trip> findAll() {
        return tripRepository.findAll();
    }

    public Optional<Trip> findById(Long id) {   //optional perchè il trip potrebbe esserci come non esserci.
        return tripRepository.findById(id);
    }

    public void deleteById(Long id) {
        tripRepository.deleteById(id);
    }

    public Trip createTrip(String name,
                       String description,
                       LocalDate startDate,
                       LocalDate endDate,
                       Double budget) {

    Trip trip = new Trip();

    trip.setName(name);
    trip.setDescription(description);
    trip.setStartDate(startDate);
    trip.setEndDate(endDate);
    trip.setBudget(budget);

    return tripRepository.save(trip);
}

public Optional<Trip> updateTrip(Long id,
                                 String name,
                                 String description,
                                 LocalDate startDate,
                                 LocalDate endDate,
                                 Double budget) {

    return tripRepository.findById(id)
            .map(trip -> {    //se il viaggio esiste, modifico quell'oggetto.

                trip.setName(name);
                trip.setDescription(description);
                trip.setStartDate(startDate);
                trip.setEndDate(endDate);
                trip.setBudget(budget);  //non lavora su un nuovo trip ma sul trip esistente già recuperato dal database.

                return tripRepository.save(trip);
            });
}

public Double calculateTotalExpenses(Trip trip) {

    double total = 0.0;

    for (Expense expense : trip.getExpenses()) {

        if (expense.getAmount() != null) {
            total += expense.getAmount();
        }
    }

    return total;
}

public Double calculateRemainingBudget(Trip trip) {

    double budget = 0.0;

    if (trip.getBudget() != null) {
        budget = trip.getBudget();
    }

    return budget - calculateTotalExpenses(trip);
}
}