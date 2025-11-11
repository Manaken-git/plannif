package fr.manaken.plannif.business;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import fr.manaken.plannif.model.Seance;

import static ai.timefold.solver.core.api.score.stream.Joiners.equal;
import static ai.timefold.solver.core.api.score.stream.Joiners.overlapping;

public class PlanningConstraints implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[] {
            noOverlapForProfesseur(factory)
        };
    }

    private Constraint noOverlapForProfesseur(ConstraintFactory factory) {
        return factory.forEach(Seance.class)
            .join(Seance.class,
                equal(Seance::getProfesseur)
            )
            .penalize("Professeur surbooké", HardSoftScore.ONE_HARD);
    }


    // Autres contraintes
}
