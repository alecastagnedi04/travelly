package it.uniroma3.siw.travelly.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import it.uniroma3.siw.travelly.model.Trip;


public interface TripRepository
        extends CrudRepository<Trip, Long> {


    /*
     * =====================================================
     * VIAGGI PERSONALI
     * =====================================================
     *
     * Restituisce solamente i viaggi personali
     * appartenenti all'utente.
     *
     * Gli itinerari ufficiali Travelly
     * vengono esclusi.
     */
    Iterable<Trip> findByOwnerUsernameAndTemplateFalse(
            String username
    );


    /*
     * Cerca un Trip tramite:
     *
     * - id
     * - username del proprietario
     *
     * Utilizzato per impedire ad un utente
     * di accedere ai viaggi degli altri utenti.
     */
    Optional<Trip> findByIdAndOwnerUsername(
            Long id,
            String username
    );



    /*
     * =====================================================
     * ITINERARI UFFICIALI TRAVELLY
     * =====================================================
     *
     * Restituisce tutti i Trip
     * con template = true.
     */
    Iterable<Trip> findByTemplateTrue();



    /*
     * Cerca uno specifico itinerario
     * ufficiale Travelly.
     */
    Optional<Trip> findByIdAndTemplateTrue(
            Long id
    );
}