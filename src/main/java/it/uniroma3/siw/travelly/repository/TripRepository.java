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
     * TEMPLATE UFFICIALI TRAVELLY
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
     *
     * template = false
     * visibility = PUBLIC
     */

    List<Trip> findByTemplateFalseAndVisibilityOrderByIdDesc(
            TripVisibility visibility
    );


    Optional<Trip> findByIdAndTemplateFalseAndVisibility(
            Long id,
            TripVisibility visibility
    );
}