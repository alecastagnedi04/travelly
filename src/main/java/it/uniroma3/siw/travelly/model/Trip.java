package it.uniroma3.siw.travelly.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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


    /*
     * Nome del file della copertina.
     */
    private String coverImage;


    /*
     * false = viaggio personale
     * true  = template ufficiale Travelly
     */
    private boolean template = false;


    /*
     * PRIVATE = visibile solo al proprietario
     * PUBLIC  = itinerario pubblico
     */
    @Enumerated(EnumType.STRING)
    private TripVisibility visibility =
            TripVisibility.PRIVATE;



    /*
     * =====================================================
     * DESTINAZIONI
     * =====================================================
     */
    @OneToMany(
            mappedBy = "trip",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Destination> destinations =
            new ArrayList<>();



    /*
     * =====================================================
     * ATTIVITÀ
     * =====================================================
     */
    @OneToMany(
            mappedBy = "trip",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Activity> activities =
            new ArrayList<>();



    /*
     * =====================================================
     * PARTECIPANTI
     * =====================================================
     */
    @OneToMany(
            mappedBy = "trip",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Participant> participants =
            new ArrayList<>();



    /*
     * =====================================================
     * SPESE
     * =====================================================
     */
    @OneToMany(
            mappedBy = "trip",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Expense> expenses =
            new ArrayList<>();



    /*
     * =====================================================
     * PROPRIETARIO
     * =====================================================
     *
     * Viaggio personale:
     * owner = User
     *
     * Template:
     * owner = null
     */
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;



    public Trip() {
    }



    public Long getId() {

        return id;
    }


    public void setId(
            Long id) {

        this.id =
                id;
    }



    public String getName() {

        return name;
    }


    public void setName(
            String name) {

        this.name =
                name;
    }



    public String getDescription() {

        return description;
    }


    public void setDescription(
            String description) {

        this.description =
                description;
    }



    public LocalDate getStartDate() {

        return startDate;
    }


    public void setStartDate(
            LocalDate startDate) {

        this.startDate =
                startDate;
    }



    public LocalDate getEndDate() {

        return endDate;
    }


    public void setEndDate(
            LocalDate endDate) {

        this.endDate =
                endDate;
    }



    public Double getBudget() {

        return budget;
    }


    public void setBudget(
            Double budget) {

        this.budget =
                budget;
    }



    public String getCoverImage() {

        return coverImage;
    }


    public void setCoverImage(
            String coverImage) {

        this.coverImage =
                coverImage;
    }



    public boolean isTemplate() {

        return template;
    }


    public void setTemplate(
            boolean template) {

        this.template =
                template;
    }



    public TripVisibility getVisibility() {

        return visibility;
    }


    public void setVisibility(
            TripVisibility visibility) {

        this.visibility =
                visibility;
    }



    public List<Destination> getDestinations() {

        return destinations;
    }


    public void setDestinations(
            List<Destination> destinations) {

        this.destinations =
                destinations;
    }



    public List<Activity> getActivities() {

        return activities;
    }


    public void setActivities(
            List<Activity> activities) {

        this.activities =
                activities;
    }



    public List<Participant> getParticipants() {

        return participants;
    }


    public void setParticipants(
            List<Participant> participants) {

        this.participants =
                participants;
    }



    public List<Expense> getExpenses() {

        return expenses;
    }


    public void setExpenses(
            List<Expense> expenses) {

        this.expenses =
                expenses;
    }



    public User getOwner() {

        return owner;
    }


    public void setOwner(
            User owner) {

        this.owner =
                owner;
    }
}