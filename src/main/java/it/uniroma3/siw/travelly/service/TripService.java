package it.uniroma3.siw.travelly.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import it.uniroma3.siw.travelly.model.Trip;
import it.uniroma3.siw.travelly.repository.TripRepository;

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
}