package it.uniroma3.siw.travelly.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import it.uniroma3.siw.travelly.model.Trip;

public interface TripRepository extends CrudRepository<Trip, Long> {

    Iterable<Trip> findByOwnerUsername(String username);

    Optional<Trip> findByIdAndOwnerUsername(
            Long id,
            String username
    );
}