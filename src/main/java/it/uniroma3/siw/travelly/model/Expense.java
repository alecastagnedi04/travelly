package it.uniroma3.siw.travelly.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

@Entity
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String description;
    private Double amount;
    private LocalDate date;


    @Enumerated(EnumType.STRING)
    private ExpenseCategory category;


    @ManyToOne
    @JoinColumn(name = "trip_id")
    private Trip trip;


    /*
     * Partecipante che ha sostenuto la spesa.
     *
     * Può inizialmente essere null, ad esempio quando
     * la spesa viene generata automaticamente da un'attività.
     */
    @ManyToOne
    @JoinColumn(name = "paid_by_id")
    private Participant paidBy;


    /*
     * Se questa Expense deriva da un'Activity,
     * memorizziamo quale Activity l'ha generata.
     *
     * unique = true impedisce che una stessa Activity
     * generi più di una Expense.
     */
    @OneToOne
    @JoinColumn(
            name = "activity_id",
            unique = true
    )
    private Activity activity;


    public Expense() {
    }


    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public String getDescription() {
        return description;
    }


    public void setDescription(String description) {
        this.description = description;
    }


    public Double getAmount() {
        return amount;
    }


    public void setAmount(Double amount) {
        this.amount = amount;
    }


    public LocalDate getDate() {
        return date;
    }


    public void setDate(LocalDate date) {
        this.date = date;
    }


    public ExpenseCategory getCategory() {
        return category;
    }


    public void setCategory(ExpenseCategory category) {
        this.category = category;
    }


    public Trip getTrip() {
        return trip;
    }


    public void setTrip(Trip trip) {
        this.trip = trip;
    }


    public Participant getPaidBy() {
        return paidBy;
    }


    public void setPaidBy(Participant paidBy) {
        this.paidBy = paidBy;
    }


    public Activity getActivity() {
        return activity;
    }


    public void setActivity(Activity activity) {
        this.activity = activity;
    }
}