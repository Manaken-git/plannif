package fr.manaken.plannif.business;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import fr.manaken.plannif.model.Seance;

import static ai.timefold.solver.core.api.score.stream.Joiners.equal;
import static ai.timefold.solver.core.api.score.stream.Joiners.lessThan;

public class PlanningConstraints implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[] {
                roomConflict(factory),
                teacherConflict(factory),
                studentGroupConflict(factory)
        };
    }

    private Constraint roomConflict(ConstraintFactory factory) {
        return factory.forEach(Seance.class)
                .join(Seance.class,
                        equal(Seance::getSalle),
                        equal(Seance::getCreneau),
                        lessThan(Seance::getId))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Room conflict");
    }

    private Constraint teacherConflict(ConstraintFactory factory) {
        return factory.forEach(Seance.class)
                .join(Seance.class,
                        equal(Seance::getProfesseur),
                        equal(Seance::getCreneau),
                        lessThan(Seance::getId))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Teacher conflict");
    }

    private Constraint studentGroupConflict(ConstraintFactory factory) {
        return factory.forEach(Seance.class)
                .join(Seance.class,
                        equal(Seance::getClasse),
                        equal(Seance::getCreneau),
                        lessThan(Seance::getId))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Student group conflict");
    }
}
