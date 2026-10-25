package it.uniroma3.siw.travelly.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import it.uniroma3.siw.travelly.model.TripVisibility;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;


public class TripForm {


    @NotBlank(
        message = "Il nome del viaggio è obbligatorio"
    )
    @Size(
        max = 100,
        message = "Il nome può contenere al massimo 100 caratteri"
    )
    private String name;



    @Size(
        max = 255,
        message = "La descrizione può contenere al massimo 255 caratteri"
    )
    private String description;



    /*
     * Gli input HTML type="date"
     * lavorano internamente con il formato:
     *
     * yyyy-MM-dd
     *
     * Esempio:
     * 2027-06-15
     */
    @NotNull(
        message = "La data di partenza è obbligatoria"
    )
    @DateTimeFormat(
        iso = DateTimeFormat.ISO.DATE
    )
    private LocalDate startDate;



    @NotNull(
        message = "La data di ritorno è obbligatoria"
    )
    @DateTimeFormat(
        iso = DateTimeFormat.ISO.DATE
    )
    private LocalDate endDate;



    @NotNull(
        message = "Il budget è obbligatorio"
    )
    @PositiveOrZero(
        message = "Il budget non può essere negativo"
    )
    private Double budget;



    @NotNull(
        message = "Seleziona la visibilità del viaggio"
    )
    private TripVisibility visibility =
            TripVisibility.PRIVATE;



    /*
     * =====================================================
     * GETTER / SETTER
     * =====================================================
     */

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



    public TripVisibility getVisibility() {

        return visibility;
    }


    public void setVisibility(
            TripVisibility visibility) {

        this.visibility =
                visibility;
    }
}