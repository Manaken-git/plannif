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
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@PlanningSolution
@Getter
@Setter
public class Planning {
    private List<Seance> seances;
    private List<Creneau> creneaux;
    private List<Salle> salles;

    private HardSoftScore score;

    @PlanningEntityCollectionProperty
    public List<Seance> getSeances() {
        return seances;
    }

    @ValueRangeProvider(id = "creneauRange")
    @ProblemFactCollectionProperty
    public List<Creneau> getCreneaux() {
        return creneaux;
    }

    @ValueRangeProvider(id = "salleRange")
    @ProblemFactCollectionProperty
    public List<Salle> getSalles() {
        return salles;
    }

    @ProblemFactCollectionProperty
    public List<fr.manaken.plannif.model.ProfesseurDayOff> getProfesseurDayOffs() {
        return professeurDayOffs;
    }

    private List<fr.manaken.plannif.model.ProfesseurDayOff> professeurDayOffs;

    @ProblemFactCollectionProperty
    public List<fr.manaken.plannif.model.ClassePresence> getClassePresences() {
        return classePresences;
    }

    private List<fr.manaken.plannif.model.ClassePresence> classePresences;

    @PlanningScore
    public HardSoftScore getScore() {
        return score;
    }
}