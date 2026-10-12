package it.uniroma3.siw.travelly.repository;

import org.springframework.data.repository.CrudRepository;

import it.uniroma3.siw.travelly.model.Trip;

public interface TripRepository extends CrudRepository<Trip, Long> {   //triprepository eredita tutte le operazioni crud previste da crudrepository.

}