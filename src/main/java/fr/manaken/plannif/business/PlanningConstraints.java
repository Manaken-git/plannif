package fr.manaken.plannif.business;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;
import fr.manaken.plannif.model.Seance;
import fr.manaken.plannif.model.MatiereClasseConfig;
import org.jspecify.annotations.NonNull;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import static ai.timefold.solver.core.api.score.stream.Joiners.equal;
import static ai.timefold.solver.core.api.score.stream.Joiners.lessThan;
import static ai.timefold.solver.core.api.score.stream.Joiners.filtering;
import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.sumBigDecimal;

public class PlanningConstraints implements ConstraintProvider {

        @Override
        public Constraint[] defineConstraints(@NonNull ConstraintFactory factory) {
                return new Constraint[] {
                                roomConflict(factory),
                                teacherConflict(factory),
                                studentGroupConflict(factory),
                                teacherDayOff(factory),
                                teacherMaxHoursPerDay(factory),
                                teacherMaxHoursPerWeek(factory),
                                teacherMaxHoursPerSession(factory),
                                teacherClassMaxHoursConsecutive(factory),
                                studentGroupPresence(factory),
                                teacherMustBeQualified(factory),
                                teacherMaxGap(factory),
                                subjectClassPeriodConstraint(factory),
                                subjectClassMaxSessionsPerDay(factory),
                                subjectClassSpreading(factory)
                };
        }

        private Constraint subjectClassPeriodConstraint(ConstraintFactory constraintFactory) {
                return constraintFactory.forEach(Seance.class)
                        .join(MatiereClasseConfig.class,
                                Joiners.equal(Seance::getMatiere, MatiereClasseConfig::getMatiere),
                                Joiners.equal(Seance::getClasse, MatiereClasseConfig::getClasse))
                        .filter((seance, config) -> {
                            if (seance.getCreneau() == null) return false;
                            java.time.LocalDate date = seance.getCreneau().getDebut().toLocalDate();
                            return date.isBefore(config.getDateDebut()) || date.isAfter(config.getDateFin());
                        })
                        .penalize(HardSoftScore.ONE_HARD)
                        .asConstraint("Subject class period");
        }

        public Constraint studentGroupPresence(ConstraintFactory factory) {
                return factory.forEach(Seance.class)
                                .filter(seance -> seance.getCreneau() != null)
                                .ifNotExists(fr.manaken.plannif.model.ClassePresence.class,
                                                equal(Seance::getClasse, fr.manaken.plannif.model.ClassePresence::getClasse),
                                                filtering((seance, presence) -> {
                                                        java.time.LocalDate date = seance.getCreneau().getDebut().toLocalDate();
                                                        return !date.isBefore(presence.getDateDebut()) && !date.isAfter(presence.getDateFin());
                                                }))
                                .penalize(HardSoftScore.ONE_HARD)
                                .asConstraint("Student group presence");
        }

        public Constraint teacherMustBeQualified(ConstraintFactory factory) {
                return factory.forEach(Seance.class)
                                .filter(seance -> seance.getProfesseur() != null
                                                && !seance.getProfesseur().getMatieres().contains(seance.getMatiere()))
                                .penalize(HardSoftScore.ONE_HARD)
                                .asConstraint("Teacher must be qualified for the subject");
        }

        public Constraint teacherClassMaxHoursConsecutive(ConstraintFactory factory) {
                var workStream = factory
                                .forEach(Seance.class)
                                .filter(seance -> seance.getProfesseur() != null && seance.getCreneau() != null)
                                .groupBy(Seance::getProfesseur, Seance::getClasse,
                                                sumBigDecimal(this::getDurationInHours))
                                .map((prof, classe, duration) -> new fr.manaken.plannif.model.TeacherClassWork(
                                                prof, classe, java.time.LocalDate.now(), duration));

                return workStream.join(workStream,
                                equal(fr.manaken.plannif.model.TeacherClassWork::getProfesseur,
                                                fr.manaken.plannif.model.TeacherClassWork::getProfesseur),
                                equal(fr.manaken.plannif.model.TeacherClassWork::getClasse,
                                                fr.manaken.plannif.model.TeacherClassWork::getClasse))
                                .filter((w1, w2) -> w1.getHours().add(w2.getHours())
                                                .compareTo(BigDecimal.valueOf(5)) > 0)
                                .penalize(HardSoftScore.ONE_HARD)
                                .asConstraint("Teacher class max 5h consecutive 2 days");
        }

        public Constraint teacherMaxHoursPerDay(ConstraintFactory factory) {
                return factory.forEach(Seance.class)
                                .filter(seance -> seance.getProfesseur() != null && seance.getCreneau() != null)
                                .groupBy(Seance::getProfesseur,
                                                seance -> seance.getCreneau().getDebut().toLocalDate(),
                                                sumBigDecimal(this::getDurationInHours))
                                .filter((prof, date, totalHours) -> prof.getMaxHeuresParJour() != null
                                                && totalHours.compareTo(prof.getMaxHeuresParJour()) > 0)
                                .penalize(HardSoftScore.ONE_HARD)
                                .asConstraint("Max hours per day for teacher");
        }

        public Constraint teacherMaxHoursPerWeek(ConstraintFactory factory) {
                var woy = java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear();
                return factory.forEach(Seance.class)
                                .filter(seance -> seance.getProfesseur() != null && seance.getCreneau() != null)
                                .groupBy(Seance::getProfesseur,
                                                seance -> seance.getCreneau().getDebut().get(woy),
                                                sumBigDecimal(this::getDurationInHours))
                                .filter((prof, week, totalHours) -> prof.getMaxHeuresParSemaine() != null
                                                && totalHours.compareTo(prof.getMaxHeuresParSemaine()) > 0)
                                .penalize(HardSoftScore.ONE_HARD)
                                .asConstraint("Max hours per week for teacher");
        }

        public Constraint teacherMaxHoursPerSession(ConstraintFactory factory) {
                return factory.forEach(Seance.class)
                                .filter(seance -> seance.getProfesseur() != null && seance.getCreneau() != null)
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
                                .filter(seance -> seance.getProfesseur() != null && seance.getCreneau() != null)
                                .join(fr.manaken.plannif.model.ProfesseurDayOff.class,
                                                equal(Seance::getProfesseur,
                                                                fr.manaken.plannif.model.ProfesseurDayOff::getProfesseur))
                                .filter((seance, dayOff) -> seance.getCreneau().getDebut().getDayOfWeek().getValue() - 1 == dayOff.getDayOfWeek())
                                .penalize(HardSoftScore.ONE_SOFT)
                                .asConstraint("Teacher day off");
        }

        public Constraint roomConflict(ConstraintFactory factory) {
                return factory.forEach(Seance.class)
                                .filter(s -> s.getSalle() != null && s.getCreneau() != null)
                                .join(Seance.class,
                                                equal(Seance::getSalle),
                                                lessThan(Seance::getId))
                                .filter((s1, s2) -> s1.getCreneau() != null && s2.getCreneau() != null
                                                && s1.getCreneau().getDebut().isBefore(s2.getCreneau().getFin())
                                                && s1.getCreneau().getFin().isAfter(s2.getCreneau().getDebut()))
                                .penalize(HardSoftScore.ONE_HARD)
                                .asConstraint("Room conflict");
        }

        public Constraint teacherConflict(ConstraintFactory factory) {
                return factory.forEach(Seance.class)
                                .filter(s -> s.getProfesseur() != null && s.getCreneau() != null)
                                .join(Seance.class,
                                                equal(Seance::getProfesseur),
                                                lessThan(Seance::getId))
                                .filter((s1, s2) -> s1.getCreneau() != null && s2.getCreneau() != null
                                                && s1.getCreneau().getDebut().isBefore(s2.getCreneau().getFin())
                                                && s1.getCreneau().getFin().isAfter(s2.getCreneau().getDebut()))
                                .penalize(HardSoftScore.ONE_HARD)
                                .asConstraint("Teacher conflict");
        }

        public Constraint studentGroupConflict(ConstraintFactory factory) {
                return factory.forEach(Seance.class)
                                .filter(s -> s.getClasse() != null && s.getCreneau() != null)
                                .join(Seance.class,
                                              equal(Seance::getClasse),
                                              lessThan(Seance::getId))
                                .filter((s1, s2) -> s1.getCreneau() != null && s2.getCreneau() != null
                                                && s1.getCreneau().getDebut().isBefore(s2.getCreneau().getFin())
                                                && s1.getCreneau().getFin().isAfter(s2.getCreneau().getDebut()))
                                .penalize(HardSoftScore.ONE_HARD)
                                .asConstraint("Student group conflict");
        }

        public Constraint teacherMaxGap(ConstraintFactory factory) {
                return factory.forEach(Seance.class)
                                .filter(s -> s.getProfesseur() != null && s.getCreneau() != null)
                                .join(Seance.class,
                                                equal(Seance::getProfesseur),
                                                lessThan(s -> s.getCreneau().getFin(), s -> s.getCreneau().getDebut()))
                                .ifNotExists(Seance.class,
                                                equal((s1, s2) -> s1.getProfesseur(), Seance::getProfesseur),
                                                filtering((s1, s2, s3) -> s3.getCreneau() != null 
                                                                && s3.getCreneau().getDebut().isAfter(s1.getCreneau().getFin())
                                                                && s3.getCreneau().getDebut().isBefore(s2.getCreneau().getDebut())))
                                .filter((s1, s2) -> ChronoUnit.MINUTES.between(s1.getCreneau().getFin(),
                                                s2.getCreneau().getDebut()) > 120)
                                .penalize(HardSoftScore.ONE_HARD)
                                .asConstraint("Teacher max gap 2h");
        }

        public Constraint subjectClassMaxSessionsPerDay(ConstraintFactory factory) {
                return factory.forEach(Seance.class)
                                .filter(seance -> seance.getClasse() != null && seance.getMatiere() != null && seance.getCreneau() != null)
                                .groupBy(Seance::getClasse,
                                                Seance::getMatiere,
                                                seance -> seance.getCreneau().getDebut().toLocalDate(),
                                                ai.timefold.solver.core.api.score.stream.ConstraintCollectors.count())
                                .filter((classe, matiere, date, count) -> count > 1)
                                .penalize(HardSoftScore.ONE_HARD)
                                .asConstraint("Subject class max sessions per day");
        }

        public Constraint subjectClassSpreading(ConstraintFactory factory) {
                return factory.forEach(Seance.class)
                                .filter(seance -> seance.getClasse() != null && seance.getMatiere() != null && seance.getCreneau() != null)
                                .join(Seance.class,
                                                equal(Seance::getClasse),
                                                equal(Seance::getMatiere),
                                                lessThan(Seance::getId))
                                .filter((s1, s2) -> {
                                        long days = Math.abs(java.time.temporal.ChronoUnit.DAYS.between(
                                                        s1.getCreneau().getDebut().toLocalDate(),
                                                        s2.getCreneau().getDebut().toLocalDate()));
                                        return days <= 1;
                                })
                                .penalize(HardSoftScore.ONE_SOFT)
                                .asConstraint("Subject class spreading penalty");
        }
}
