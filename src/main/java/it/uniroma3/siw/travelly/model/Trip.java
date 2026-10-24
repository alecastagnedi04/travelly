package it.uniroma3.siw.travelly.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
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
     * Nome del file utilizzato come
     * immagine di copertina.
     *
     * Nel database viene salvato solamente
     * il nome del file.
     */
    private String coverImage;


    /*
     * Indica se il viaggio è un itinerario
     * ufficiale creato da Travelly.
     *
     * false = viaggio personale
     * true  = itinerario/template ufficiale
     */
    private boolean template = false;



    /*
     * =====================================================
     * DESTINAZIONI
     * =====================================================
     *
     * Un Trip può avere più Destination.
     *
     * Se il Trip viene eliminato,
     * vengono eliminate anche le sue destinazioni.
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
     * Molti Trip possono appartenere allo stesso User.
     *
     * Per i normali viaggi personali:
     *
     * owner = utente
     *
     * Per gli itinerari ufficiali Travelly:
     *
     * owner = null
     */
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;



    /*
     * Costruttore vuoto richiesto da JPA.
     */
    public Trip() {
    }



    /* =====================================================
     * ID
     * ===================================================== */

    public Long getId() {

        return id;
    }


    public void setId(
            Long id) {

        this.id = id;
    }



    /* =====================================================
     * NAME
     * ===================================================== */

    public String getName() {

        return name;
    }


    public void setName(
            String name) {

        this.name = name;
    }



    /* =====================================================
     * DESCRIPTION
     * ===================================================== */

    public String getDescription() {

        return description;
    }


    public void setDescription(
            String description) {

        this.description =
                description;
    }



    /* =====================================================
     * START DATE
     * ===================================================== */

    public LocalDate getStartDate() {

        return startDate;
    }


    public void setStartDate(
            LocalDate startDate) {

        this.startDate =
                startDate;
    }



    /* =====================================================
     * END DATE
     * ===================================================== */

    public LocalDate getEndDate() {

        return endDate;
    }


    public void setEndDate(
            LocalDate endDate) {

        this.endDate =
                endDate;
    }



    /* =====================================================
     * BUDGET
     * ===================================================== */

    public Double getBudget() {

        return budget;
    }


    public void setBudget(
            Double budget) {

        this.budget =
                budget;
    }



    /* =====================================================
     * COVER IMAGE
     * ===================================================== */

    public String getCoverImage() {

        return coverImage;
    }


    public void setCoverImage(
            String coverImage) {

        this.coverImage =
                coverImage;
    }



    /* =====================================================
     * TEMPLATE
     * ===================================================== */

    public boolean isTemplate() {

        return template;
    }


    public void setTemplate(
            boolean template) {

        this.template =
                template;
    }



    /* =====================================================
     * DESTINATIONS
     * ===================================================== */

    public List<Destination> getDestinations() {

        return destinations;
    }


    public void setDestinations(
            List<Destination> destinations) {

        this.destinations =
                destinations;
    }



    /* =====================================================
     * ACTIVITIES
     * ===================================================== */

    public List<Activity> getActivities() {

        return activities;
    }


    public void setActivities(
            List<Activity> activities) {

        this.activities =
                activities;
    }



    /* =====================================================
     * PARTICIPANTS
     * ===================================================== */

    public List<Participant> getParticipants() {

        return participants;
    }


    public void setParticipants(
            List<Participant> participants) {

        this.participants =
                participants;
    }



    /* =====================================================
     * EXPENSES
     * ===================================================== */

    public List<Expense> getExpenses() {

        return expenses;
    }


    public void setExpenses(
            List<Expense> expenses) {

        this.expenses =
                expenses;
    }



    /* =====================================================
     * OWNER
     * ===================================================== */

    public User getOwner() {

        return owner;
    }


    public void setOwner(
            User owner) {

        this.owner =
                owner;
    }
}