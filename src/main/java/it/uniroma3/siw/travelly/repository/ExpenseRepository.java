package it.uniroma3.siw.travelly.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import it.uniroma3.siw.travelly.model.Expense;

public interface ExpenseRepository
        extends CrudRepository<Expense, Long> {

    Optional<Expense> findByActivityId(Long activityId);

    Iterable<Expense> findByPaidById(Long participantId);
}  //mi permette di chiedere se esiste una expense collegata all'activity, in modo da non poter eliminare un'attività se ha una spesa collegata