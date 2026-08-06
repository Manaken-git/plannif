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

    // @TODO Ajouter les toString sur les classe PlanningVariable pour savoir d'ou viennent les hard/Soft points
    
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

    public enum TypeSeance {
        COURS, TP, EXAMEN, VIE_DE_CLASSE
    }
}
