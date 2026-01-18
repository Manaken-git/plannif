package fr.manaken.plannif.business;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import fr.manaken.plannif.model.Creneau;
import fr.manaken.plannif.model.Salle;
import fr.manaken.plannif.model.Seance;
import fr.manaken.plannif.model.Professeur;
import fr.manaken.plannif.model.Classe;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@PlanningSolution
@Getter
@Setter
public class Planning {

    @PlanningEntityCollectionProperty
    private List<Seance> seances = new java.util.ArrayList<>();

    @ValueRangeProvider(id = "creneauRange")
    @ProblemFactCollectionProperty
    private List<Creneau> creneaux = new java.util.ArrayList<>();

    @ValueRangeProvider(id = "salleRange")
    @ProblemFactCollectionProperty
    private List<Salle> salles = new java.util.ArrayList<>();

    @ProblemFactCollectionProperty
    private List<Professeur> professeurs = new java.util.ArrayList<>();

    @ProblemFactCollectionProperty
    private List<Classe> classes = new java.util.ArrayList<>();

    @ProblemFactCollectionProperty
    private List<fr.manaken.plannif.model.ProfesseurDayOff> professeurDayOffs = new java.util.ArrayList<>();

    @ProblemFactCollectionProperty
    private List<fr.manaken.plannif.model.ClassePresence> classePresences = new java.util.ArrayList<>();

    @PlanningScore
    private HardSoftScore score;

}