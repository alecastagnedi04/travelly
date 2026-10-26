package it.uniroma3.siw.travelly.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import it.uniroma3.siw.travelly.model.Expense;


public interface ExpenseRepository
        extends CrudRepository<Expense, Long> {


    /*
     * =====================================================
     * EXPENSE COLLEGATA AD UNA ACTIVITY
     * =====================================================
     *
     * Serve per verificare se esiste una Expense
     * generata automaticamente da una Activity.
     */
    Optional<Expense> findByActivityId(
            Long activityId
    );



    /*
     * =====================================================
     * EXPENSE PAGATE DA UN PARTECIPANTE
     * =====================================================
     *
     * Utilizzato quando viene eliminato
     * un Participant.
     *
     * Le Expense non vengono eliminate:
     * paidBy viene impostato a null.
     */
    Iterable<Expense> findByPaidById(
            Long participantId
    );



    /*
     * =====================================================
     * TOTALE SPESE DI UN VIAGGIO
     * =====================================================
     *
     * Query JPQL esplicita.
     *
     * SUM viene eseguito direttamente
     * dal database invece di caricare
     * tutte le Expense e sommarle in Java.
     *
     * COALESCE permette di ottenere 0.0
     * se il viaggio non possiede ancora spese.
     */
    @Query("""
        SELECT COALESCE(SUM(e.amount), 0.0)
        FROM Expense e
        WHERE e.trip.id = :tripId
        """)
    Double sumAmountByTripId(
            @Param("tripId")
            Long tripId
    );
} //mi permette di chiedere se esiste una expense collegata all'activity, in modo da non poter eliminare un'attività se ha una spesa collegata