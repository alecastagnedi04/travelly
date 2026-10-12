package it.uniroma3.siw.travelly.service;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.stereotype.Service;

import it.uniroma3.siw.travelly.model.Destination;
import it.uniroma3.siw.travelly.repository.DestinationRepository;
import it.uniroma3.siw.travelly.repository.TripRepository;

@Service
public class DestinationService {

    private final DestinationRepository destinationRepository;   //serve per salvare destination
    private final TripRepository tripRepository;  //serve per trovare il trip a cui collegarla.

    public DestinationService(DestinationRepository destinationRepository,
                              TripRepository tripRepository) {

        this.destinationRepository = destinationRepository;
        this.tripRepository = tripRepository;
    }

    public Optional<Destination> createDestination(
            Long tripId,
            String city,
            String country,
            LocalDate arrivalDate,
            LocalDate departureDate) {

        return tripRepository.findById(tripId)
                .map(trip -> {

                    Destination destination = new Destination();

                    destination.setCity(city);
                    destination.setCountry(country);
                    destination.setArrivalDate(arrivalDate);
                    destination.setDepartureDate(departureDate);

                    destination.setTrip(trip);

                    return destinationRepository.save(destination);
                });
    }
}