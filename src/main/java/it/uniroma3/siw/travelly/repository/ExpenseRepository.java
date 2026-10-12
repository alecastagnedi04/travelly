package it.uniroma3.siw.travelly.repository;

import org.springframework.data.repository.CrudRepository;

import it.uniroma3.siw.travelly.model.Expense;

public interface ExpenseRepository extends CrudRepository<Expense, Long> {

}