package it.uniroma3.siw.travelly.repository;

import org.springframework.data.repository.CrudRepository;

import it.uniroma3.siw.travelly.model.Participant;

public interface ParticipantRepository extends CrudRepository<Participant, Long> {

}