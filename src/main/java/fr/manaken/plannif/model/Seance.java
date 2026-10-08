package fr.manaken.plannif.model;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;

@Getter
@Setter

@PlanningEntity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Seance {

    @PlanningId
    @EqualsAndHashCode.Include
    private Long id;

    
    
    @PlanningVariable(valueRangeProviderRefs = "professeurRange")
    private Professeur professeur;

    
    
    private Classe classe;

    
    
    private Matiere matiere;

    
    
    @PlanningVariable(valueRangeProviderRefs = "salleRange")
    private Salle salle;

    
    
    @PlanningVariable(valueRangeProviderRefs = "dateDebutRange")
    private java.time.LocalDateTime debut;

    private java.time.LocalDateTime fin;

    private Integer dureeMinutes;
    private Integer dureeAttendueMinutes;

    private String typeSalleRequis;
    private Long alignementId;

    private TypeSeance type;

    public String getRequiredRoomType() {
        return typeSalleRequis;
    }

    public int getExpectedDurationMinutes() {
        if (dureeAttendueMinutes != null) {
            return dureeAttendueMinutes;
        }
        if (type == TypeSeance.TP) {
            return 90;
        }
        return 60;
    }

    public long getActualDurationMinutes() {
        if (this.debut == null || this.fin == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.MINUTES.between(this.debut, this.fin);
    }

    public int getDureeMinutes() {
        if (dureeMinutes != null) {
            return dureeMinutes;
        }
        return getExpectedDurationMinutes();
    }

    public void setDebut(java.time.LocalDateTime debut) {
        this.debut = debut;
        if (debut != null) {
            this.fin = debut.plusMinutes(getDureeMinutes());
        } else {
            this.fin = null;
        }
    }

    public java.time.LocalDateTime getFin() {
        if (this.debut == null) {
            return null;
        }
        if (this.fin != null && this.dureeMinutes != null
                && java.time.temporal.ChronoUnit.MINUTES.between(this.debut, this.fin) == this.dureeMinutes) {
            return this.fin;
        }
        return this.debut.plusMinutes(getDureeMinutes());
    }

    public void setFin(java.time.LocalDateTime fin) {
        this.fin = fin;
        if (this.debut != null && fin != null) {
            this.dureeMinutes = (int) java.time.temporal.ChronoUnit.MINUTES.between(this.debut, fin);
        }
    }

    public void setType(TypeSeance type) {
        this.type = type;
        if (dureeMinutes == null && this.debut != null) {
            this.fin = this.debut.plusMinutes(getDureeMinutes());
        }
    }

    @Override
    public String toString() {
        return "Seance{" +
                "id=" + id +
                ", professeur=" + professeur +
                ", classe=" + classe +
                ", matiere=" + matiere +
                ", salle=" + salle +
                ", debut=" + debut +
                ", fin=" + fin +
                ", type=" + type +
                '}';
    }

    public enum TypeSeance {
        COURS, TP, EXAMEN, VIE_DE_CLASSE
    }
}
