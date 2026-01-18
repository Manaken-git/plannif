package fr.manaken.plannif.model;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;

@Getter
@Setter
@Entity
@PlanningEntity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Seance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @PlanningId
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "professeur_id")
    private Professeur professeur;

    @ManyToOne
    @JoinColumn(name = "classe_id")
    private Classe classe;

    @ManyToOne
    @JoinColumn(name = "matiere_id")
    private Matiere matiere;

    @ManyToOne
    @JoinColumn(name = "salle_id")
    @PlanningVariable(valueRangeProviderRefs = "salleRange")
    private Salle salle;

    @OneToOne
    @JoinColumn(name = "creneau_id")
    @PlanningVariable(valueRangeProviderRefs = "creneauRange")
    private Creneau creneau;

    @Enumerated(EnumType.STRING)
    private TypeSeance type;

    public enum TypeSeance {
        COURS, TP, EXAMEN
    }

}
