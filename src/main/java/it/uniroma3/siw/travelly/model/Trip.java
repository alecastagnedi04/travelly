package it.uniroma3.siw.travelly.model; 

import java.time.LocalDate;  //per la data del viaggio
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;  //per le persistenze
import jakarta.persistence.OneToMany;

@Entity
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double budget;

    @OneToMany(mappedBy = "trip")
    private List<Destination> destinations = new ArrayList<>();

    @OneToMany(mappedBy = "trip")
    private List<Activity> activities = new ArrayList<>();

    @OneToMany(mappedBy = "trip")
    private List<Participant> participants = new ArrayList<>();

    public Trip() {
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Double getBudget() {
        return budget;
    }

    public void setBudget(Double budget) {
        this.budget = budget;
    }

    
    public List<Destination> getDestinations() {
    return destinations;
}
    public void setDestinations(List<Destination> destinations) {
    this.destinations = destinations;
}
    

    public List<Activity> getActivities() {
    return activities;
}
    public void setActivities(List<Activity> activities) {
    this.activities = activities;
}

    public List<Participant> getParticipants() {
    return participants;
}
    public void setParticipants(List<Participant> participants) {
    this.participants = participants;
}
}