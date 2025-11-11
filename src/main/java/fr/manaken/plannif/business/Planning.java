package fr.manaken.plannif.business;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import fr.manaken.plannif.model.Creneau;
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

    @PlanningScore
    public HardSoftScore getScore() {
        return score;
    }
}