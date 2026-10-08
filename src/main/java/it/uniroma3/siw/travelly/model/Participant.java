package it.uniroma3.siw.travelly.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.OneToMany;

@Entity
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String email;

    @ManyToOne  //perchè molti partecipanti possono appartenere a un solo trip.
    @JoinColumn(name = "trip_id")
    private Trip trip;

    @OneToMany(mappedBy = "paidBy")
    private List<Expense> paidExpenses = new ArrayList<>();

    public Participant() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Trip getTrip() {
        return trip;
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
    }

    
    public List<Expense> getPaidExpenses() {
    return paidExpenses;
}
    public void setPaidExpenses(List<Expense> paidExpenses) {
    this.paidExpenses = paidExpenses;
}
}