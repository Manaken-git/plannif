package fr.manaken.plannif.business;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import fr.manaken.plannif.model.Seance;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import static ai.timefold.solver.core.api.score.stream.Joiners.equal;
import static ai.timefold.solver.core.api.score.stream.Joiners.lessThan;
import static ai.timefold.solver.core.api.score.stream.Joiners.filtering;
import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.sumBigDecimal;

public class PlanningConstraints implements ConstraintProvider {

        @Override
        public Constraint[] defineConstraints(ConstraintFactory factory) {
                return new Constraint[] {
                                roomConflict(factory),
                                teacherConflict(factory),
                                studentGroupConflict(factory),
                                teacherDayOff(factory),
                                teacherMaxHoursPerDay(factory),
                                teacherMaxHoursPerWeek(factory),
                                teacherMaxHoursPerSession(factory),
                                teacherClassMaxHoursConsecutive(factory),
                                studentGroupPresence(factory)
                };
        }

        public Constraint studentGroupPresence(ConstraintFactory factory) {
                return factory.forEach(Seance.class)
                                .ifNotExists(fr.manaken.plannif.model.ClassePresence.class,
                                                equal(Seance::getClasse,
                                                                fr.manaken.plannif.model.ClassePresence::getClasse),
                                                filtering((seance, presence) -> !seance.getCreneau().getDebut()
                                                                .toLocalDate().isBefore(presence.getDateDebut())
                                                                && !seance.getCreneau().getDebut().toLocalDate()
                                                                                .isAfter(presence.getDateFin())))
                                .penalize(HardSoftScore.ONE_HARD)
                                .asConstraint("Student group presence");
        }

        public Constraint teacherClassMaxHoursConsecutive(ConstraintFactory factory) {
                var workStream = factory
                                .forEach(Seance.class)
                                .groupBy(Seance::getProfesseur, Seance::getClasse,
                                                seance -> seance.getCreneau().getDebut().toLocalDate(),
                                                sumBigDecimal(this::getDurationInHours))
                                .map((prof, classe, date, duration) -> new fr.manaken.plannif.model.TeacherClassWork(
                                                prof, classe, date, duration));

                return workStream.join(workStream,
                                equal(fr.manaken.plannif.model.TeacherClassWork::getProfesseur,
                                                fr.manaken.plannif.model.TeacherClassWork::getProfesseur),
                                equal(fr.manaken.plannif.model.TeacherClassWork::getClasse,
                                                fr.manaken.plannif.model.TeacherClassWork::getClasse),
                                equal(w -> w.getDate().plusDays(1), fr.manaken.plannif.model.TeacherClassWork::getDate))
                                .filter((w1, w2) -> w1.getHours().add(w2.getHours())
                                                .compareTo(BigDecimal.valueOf(5)) > 0)
                                .penalize(HardSoftScore.ONE_HARD)
                                .asConstraint("Teacher class max 5h consecutive 2 days");
        }

        public Constraint teacherMaxHoursPerDay(ConstraintFactory factory) {
                return factory.forEach(Seance.class)
                                .groupBy(Seance::getProfesseur,
                                                seance -> seance.getCreneau().getDebut().toLocalDate(),
                                                sumBigDecimal(this::getDurationInHours))
                                .filter((prof, date, totalHours) -> prof.getMaxHeuresParJour() != null
                                                && totalHours.compareTo(prof.getMaxHeuresParJour()) > 0)
                                .penalize(HardSoftScore.ONE_HARD)
                                .asConstraint("Max hours per day for teacher");
        }

        public Constraint teacherMaxHoursPerWeek(ConstraintFactory factory) {
                return factory.forEach(Seance.class)
                                .groupBy(Seance::getProfesseur,
                                                seance -> seance.getCreneau().getDebut().get(
                                                                java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR),
                                                sumBigDecimal(this::getDurationInHours))
                                .filter((prof, week, totalHours) -> prof.getMaxHeuresParSemaine() != null
                                                && totalHours.compareTo(prof.getMaxHeuresParSemaine()) > 0)
                                .penalize(HardSoftScore.ONE_HARD)
                                .asConstraint("Max hours per week for teacher");
        }

        public Constraint teacherMaxHoursPerSession(ConstraintFactory factory) {
                return factory.forEach(Seance.class)
                                .filter(seance -> {
                                        BigDecimal duration = getDurationInHours(seance);
                                        return seance.getProfesseur().getMaxHeuresParSeance() != null
                                                        && duration.compareTo(seance.getProfesseur()
                                                                        .getMaxHeuresParSeance()) > 0;
                                })
                                .penalize(HardSoftScore.ONE_HARD)
                                .asConstraint("Max hours per session for teacher");
        }

        private BigDecimal getDurationInHours(Seance seance) {
                long minutes = ChronoUnit.MINUTES.between(seance.getCreneau().getDebut(), seance.getCreneau().getFin());
                return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP);
        }

        public Constraint teacherDayOff(ConstraintFactory factory) {
                return factory.forEach(Seance.class)
                                .join(fr.manaken.plannif.model.ProfesseurDayOff.class,
                                                equal(Seance::getProfesseur,
                                                                fr.manaken.plannif.model.ProfesseurDayOff::getProfesseur),
                                                equal(seance -> seance.getCreneau().getDebut().getDayOfWeek().getValue()
                                                                - 1,
                                                                fr.manaken.plannif.model.ProfesseurDayOff::getDayOfWeek))
                                .penalize(HardSoftScore.ONE_SOFT)
                                .asConstraint("Teacher day off");
        }

        public Constraint roomConflict(ConstraintFactory factory) {
                return factory.forEach(Seance.class)
                                .join(Seance.class,
                                                equal(Seance::getSalle),
                                                equal(Seance::getCreneau),
                                                lessThan(Seance::getId))
                                .penalize(HardSoftScore.ONE_HARD)
                                .asConstraint("Room conflict");
        }

        public Constraint teacherConflict(ConstraintFactory factory) {
                return factory.forEach(Seance.class)
                                .join(Seance.class,
                                                equal(Seance::getProfesseur),
                                                equal(Seance::getCreneau),
                                                lessThan(Seance::getId))
                                .penalize(HardSoftScore.ONE_HARD)
                                .asConstraint("Teacher conflict");
        }

        public Constraint studentGroupConflict(ConstraintFactory factory) {
                return factory.forEach(Seance.class)
                                .join(Seance.class,
                                                equal(Seance::getClasse),
                                                equal(Seance::getCreneau),
                                                lessThan(Seance::getId))
                                .penalize(HardSoftScore.ONE_HARD)
                                .asConstraint("Student group conflict");
        }
}
