package it.uniroma3.siw.travelly.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import it.uniroma3.siw.travelly.model.Trip;
import it.uniroma3.siw.travelly.model.TripVisibility;


public interface TripRepository
        extends CrudRepository<Trip, Long> {


    /*
     * =====================================================
     * VIAGGI PERSONALI
     * =====================================================
     */

    Iterable<Trip> findByOwnerUsernameAndTemplateFalse(
            String username
    );


    Optional<Trip> findByIdAndOwnerUsername(
            Long id,
            String username
    );



    /*
     * =====================================================
     * TEMPLATE UFFICIALI
     * =====================================================
     */

    Iterable<Trip> findByTemplateTrue();


    Optional<Trip> findByIdAndTemplateTrue(
            Long id
    );



    /*
     * =====================================================
     * VIAGGI PUBBLICI DELLA COMMUNITY
     * =====================================================
     */

    List<Trip> findByTemplateFalseAndVisibilityOrderByIdDesc(
            TripVisibility visibility
    );


    Optional<Trip> findByIdAndTemplateFalseAndVisibility(
            Long id,
            TripVisibility visibility
    );



    /*
     * =====================================================
     * RICERCA TEMPLATE PER NOME ESATTO
     * =====================================================
     *
     * IgnoreCase:
     * non distingue maiuscole e minuscole.
     *
     * Esempio:
     *
     * Copenhagen in 3 giorni
     *
     * viene trovato anche cercando:
     *
     * copenhagen in 3 giorni
     *
     * La ricerca NON è parziale.
     */
    List<Trip> findByTemplateTrueAndNameIgnoreCase(
            String name
    );



    /*
     * =====================================================
     * RICERCA VIAGGI PUBBLICI PER NOME ESATTO
     * =====================================================
     *
     * Filtriamo SEMPRE:
     *
     * template = false
     * visibility = PUBLIC
     *
     * In questo modo non esponiamo
     * i viaggi privati.
     */
    List<Trip>
    findByTemplateFalseAndVisibilityAndNameIgnoreCaseOrderByIdDesc(
            TripVisibility visibility,
            String name
    );
}