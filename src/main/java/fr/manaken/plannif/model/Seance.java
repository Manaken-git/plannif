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

    
    
    @PlanningVariable(valueRangeProviderRefs = "creneauRange")
    private Creneau creneau;

    
    private TypeSeance type;

    @Override
    public String toString() {
        return "Seance{" +
                "id=" + id +
                ", professeur=" + professeur +
                ", classe=" + classe +
                ", matiere=" + matiere +
                ", salle=" + salle +
                ", creneau=" + creneau +
                ", type=" + type +
                '}';
    }

    public enum TypeSeance {
        COURS, TP, EXAMEN, VIE_DE_CLASSE
    }
}
