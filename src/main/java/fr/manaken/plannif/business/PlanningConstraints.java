package fr.manaken.plannif.business;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;
import fr.manaken.plannif.model.ClassePresence;
import fr.manaken.plannif.model.DistanceSalle;
import fr.manaken.plannif.model.MatiereClasseConfig;
import fr.manaken.plannif.model.ProfesseurDayOff;
import fr.manaken.plannif.model.ProfesseurIndisponibilite;
import fr.manaken.plannif.model.Seance;
import fr.manaken.plannif.model.TeacherClassWork;
import fr.manaken.plannif.model.Vacances;
import org.jspecify.annotations.NonNull;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;

import static ai.timefold.solver.core.api.score.stream.Joiners.equal;
import static ai.timefold.solver.core.api.score.stream.Joiners.filtering;
import static ai.timefold.solver.core.api.score.stream.Joiners.greaterThan;
import static ai.timefold.solver.core.api.score.stream.Joiners.lessThan;
import static fr.manaken.plannif.business.PlanningConstraintConfiguration.*;

public class PlanningConstraints implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(@NonNull ConstraintFactory factory) {
        return new Constraint[] {
            // Contraintes de base
            roomConflict(factory),
            teacherConflict(factory),
            studentGroupConflict(factory),
            teacherDayOff(factory),
            teacherMaxHoursPerDay(factory),
            teacherMaxHoursPerWeek(factory),
            teacherMaxHoursPerSession(factory),
            teacherClassMaxHoursOverTwoDays(factory),
            studentGroupPresence(factory),
            teacherMustBeQualified(factory),
            vieDeClasseTimingConstraint(factory),
            vieDeClasseReferentConstraint(factory),
            vieDeClassePrincipalReferentPreference(factory),
            teacherMaxGap(factory),
            subjectClassPeriodConstraint(factory),
            subjectClassMaxSessionsPerDay(factory),
            subjectClassSpreading(factory),
            holidayConflict(factory),
            seanceTypeDurationMatch(factory),

            // Phase 3: Salles & Équipements
            roomCapacity(factory),
            roomEquipmentMatch(factory),

            // Phase 4: Rythme Élèves
            studentLunchBreak(factory),
            studentMaxHoursPerDay(factory),
            studentMaxGap(factory),
            studentDayStartTime(factory),
            alignedSessions(factory),

            // Phase 5: Enseignants Avancés & Déplacements
            teacherLunchBreak(factory),
            teacherUnavailability(factory),
            teacherTravelDistance(factory),

            // Phase 6: Organisation Pédagogique
            highCognitiveLoad(factory),
            sessionPairing(factory)
        };
    }

    // ==========================================
    // Phase 1 & 2 : Contraintes existantes refactorisées
    // ==========================================

    public Constraint seanceTypeDurationMatch(ConstraintFactory factory) {
        return factory.forEach(Seance.class)
                .filter(seance -> seance.getDebut() != null && seance.getFin() != null)
                .filter(seance -> seance.getActualDurationMinutes() != seance.getExpectedDurationMinutes())
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint(SEANCE_TYPE_DURATION_MATCH);
    }

    public Constraint holidayConflict(ConstraintFactory factory) {
        return factory.forEach(Seance.class)
                .filter(seance -> seance.getDebut() != null)
                .join(Vacances.class,
                        filtering((seance, vacance) -> {
                            LocalDate date = seance.getDebut().toLocalDate();
                            return !date.isBefore(vacance.getDateDebut()) && !date.isAfter(vacance.getDateFin());
                        }))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint(HOLIDAY_CONFLICT);
    }

    public Constraint subjectClassPeriodConstraint(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Seance.class)
                .join(MatiereClasseConfig.class,
                        Joiners.equal(Seance::getMatiere, MatiereClasseConfig::getMatiere),
                        Joiners.equal(Seance::getClasse, MatiereClasseConfig::getClasse))
                .filter((seance, config) -> {
                    if (seance.getDebut() == null) return false;
                    LocalDate date = seance.getDebut().toLocalDate();
                    return date.isBefore(config.getDateDebut()) || date.isAfter(config.getDateFin());
                })
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint(SUBJECT_CLASS_PERIOD);
    }

    public Constraint studentGroupPresence(ConstraintFactory factory) {
        return factory.forEach(Seance.class)
                .filter(seance -> seance.getDebut() != null)
                .ifNotExists(ClassePresence.class,
                        equal(Seance::getClasse, ClassePresence::getClasse),
                        filtering((seance, presence) -> {
                            LocalDate date = seance.getDebut().toLocalDate();
                            return !date.isBefore(presence.getDateDebut()) && !date.isAfter(presence.getDateFin());
                        }))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint(STUDENT_GROUP_PRESENCE);
    }

    public Constraint teacherMustBeQualified(ConstraintFactory factory) {
        return factory.forEach(Seance.class)
                .filter(seance -> seance.getProfesseur() != null
                        && seance.getType() != Seance.TypeSeance.VIE_DE_CLASSE
                        && (seance.getMatiere() == null || !"Vie de classe".equalsIgnoreCase(seance.getMatiere().getNom()))
                        && !seance.getProfesseur().getMatieres().contains(seance.getMatiere()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint(TEACHER_MUST_BE_QUALIFIED);
    }

    public Constraint teacherClassMaxHoursOverTwoDays(ConstraintFactory factory) {
        var workStream = factory
                .forEach(Seance.class)
                .filter(seance -> seance.getProfesseur() != null && seance.getDebut() != null && seance.getFin() != null)
                .groupBy(Seance::getProfesseur, Seance::getClasse,
                        seance -> seance.getDebut().toLocalDate(),
                        ConstraintCollectors.sumLong(this::getDurationInMinutes))
                .map(TeacherClassWork::new);

        return workStream.join(workStream,
                        equal(TeacherClassWork::getProfesseur, TeacherClassWork::getProfesseur),
                        equal(TeacherClassWork::getClasse, TeacherClassWork::getClasse),
                        equal(w -> w.getDate().plusDays(1), TeacherClassWork::getDate))
                .filter((w1, w2) -> (w1.getMinutes() + w2.getMinutes()) > 300)
                .penalize(HardSoftScore.ONE_SOFT, (w1, w2) -> (int) ((w1.getMinutes() + w2.getMinutes()) - 300))
                .asConstraint(TEACHER_CLASS_MAX_HOURS_OVER_TWO_DAYS);
    }

    public Constraint teacherClassMaxHoursConsecutive(ConstraintFactory factory) {
        return teacherClassMaxHoursOverTwoDays(factory);
    }

    public Constraint teacherMaxHoursPerDay(ConstraintFactory factory) {
        return factory.forEach(Seance.class)
                .filter(seance -> seance.getProfesseur() != null && seance.getDebut() != null && seance.getFin() != null)
                .groupBy(Seance::getProfesseur,
                        seance -> seance.getDebut().toLocalDate(),
                        ConstraintCollectors.sumLong(this::getDurationInMinutes))
                .filter((prof, date, totalMinutes) -> prof.getMaxMinutesParJour() != null
                        && totalMinutes > prof.getMaxMinutesParJour())
                .penalize(HardSoftScore.ONE_SOFT, (prof, date, totalMinutes) -> (int) (totalMinutes - prof.getMaxMinutesParJour()))
                .asConstraint(TEACHER_MAX_HOURS_PER_DAY);
    }

    public Constraint teacherMaxHoursPerWeek(ConstraintFactory factory) {
        var woy = WeekFields.ISO.weekOfWeekBasedYear();
        return factory.forEach(Seance.class)
                .filter(seance -> seance.getProfesseur() != null && seance.getDebut() != null && seance.getFin() != null)
                .groupBy(Seance::getProfesseur,
                        seance -> seance.getDebut().get(woy),
                        ConstraintCollectors.sumLong(this::getDurationInMinutes))
                .filter((prof, week, totalMinutes) -> prof.getMaxMinutesParSemaine() != null
                        && totalMinutes > prof.getMaxMinutesParSemaine())
                .penalize(HardSoftScore.ONE_SOFT, (prof, week, totalMinutes) -> (int) (totalMinutes - prof.getMaxMinutesParSemaine()))
                .asConstraint(TEACHER_MAX_HOURS_PER_WEEK);
    }

    public Constraint teacherMaxHoursPerSession(ConstraintFactory factory) {
        return factory.forEach(Seance.class)
                .filter(seance -> seance.getProfesseur() != null && seance.getDebut() != null && seance.getFin() != null)
                .filter(seance -> {
                    long duration = getDurationInMinutes(seance);
                    return seance.getProfesseur().getMaxMinutesParSeance() != null
                            && duration > seance.getProfesseur().getMaxMinutesParSeance();
                })
                .penalize(HardSoftScore.ONE_SOFT, seance -> (int) (getDurationInMinutes(seance) - seance.getProfesseur().getMaxMinutesParSeance()))
                .asConstraint(TEACHER_MAX_HOURS_PER_SESSION);
    }

    private long getDurationInMinutes(Seance seance) {
        if (seance.getDebut() == null || seance.getFin() == null) {
            return 0L;
        }
        return ChronoUnit.MINUTES.between(seance.getDebut(), seance.getFin());
    }

    public Constraint teacherDayOff(ConstraintFactory factory) {
        return factory.forEach(Seance.class)
                .filter(seance -> seance.getProfesseur() != null && seance.getDebut() != null)
                .join(ProfesseurDayOff.class,
                        equal(Seance::getProfesseur, ProfesseurDayOff::getProfesseur))
                .filter((seance, dayOff) -> seance.getDebut().getDayOfWeek().getValue() - 1 == dayOff.getDayOfWeek())
                .penalize(HardSoftScore.ONE_SOFT)
                .asConstraint(TEACHER_DAY_OFF);
    }

    public Constraint roomConflict(ConstraintFactory factory) {
        return factory.forEach(Seance.class)
                .filter(s -> s.getSalle() != null && s.getDebut() != null && s.getFin() != null)
                .join(Seance.class,
                        equal(Seance::getSalle),
                        equal(s -> s.getDebut() != null ? s.getDebut().toLocalDate() : null),
                        lessThan(Seance::getId))
                .filter((s1, s2) -> s1.getDebut() != null && s1.getFin() != null
                        && s2.getDebut() != null && s2.getFin() != null
                        && s1.getDebut().isBefore(s2.getFin())
                        && s1.getFin().isAfter(s2.getDebut()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint(ROOM_CONFLICT);
    }

    public Constraint teacherConflict(ConstraintFactory factory) {
        return factory.forEach(Seance.class)
                .filter(s -> s.getProfesseur() != null && s.getDebut() != null && s.getFin() != null)
                .join(Seance.class,
                        equal(Seance::getProfesseur),
                        equal(s -> s.getDebut() != null ? s.getDebut().toLocalDate() : null),
                        lessThan(Seance::getId))
                .filter((s1, s2) -> s1.getDebut() != null && s1.getFin() != null
                        && s2.getDebut() != null && s2.getFin() != null
                        && s1.getDebut().isBefore(s2.getFin())
                        && s1.getFin().isAfter(s2.getDebut()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint(TEACHER_CONFLICT);
    }

    public Constraint studentGroupConflict(ConstraintFactory factory) {
        return factory.forEach(Seance.class)
                .filter(s -> s.getClasse() != null && s.getDebut() != null && s.getFin() != null)
                .join(Seance.class,
                        equal(Seance::getClasse),
                        equal(s -> s.getDebut() != null ? s.getDebut().toLocalDate() : null),
                        lessThan(Seance::getId))
                .filter((s1, s2) -> s1.getDebut() != null && s1.getFin() != null
                        && s2.getDebut() != null && s2.getFin() != null
                        && s1.getDebut().isBefore(s2.getFin())
                        && s1.getFin().isAfter(s2.getDebut()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint(STUDENT_GROUP_CONFLICT);
    }

    public Constraint teacherMaxGap(ConstraintFactory factory) {
        return factory.forEach(Seance.class)
                .filter(s -> s.getProfesseur() != null && s.getDebut() != null && s.getFin() != null)
                .join(Seance.class,
                        equal(Seance::getProfesseur),
                        equal(s -> s.getFin().toLocalDate(), s -> s.getDebut().toLocalDate()),
                        lessThan(Seance::getFin, Seance::getDebut))
                .ifNotExists(Seance.class,
                        equal((s1, s2) -> s1.getProfesseur(), Seance::getProfesseur),
                        equal((s1, s2) -> s1.getFin().toLocalDate(), s -> s.getDebut().toLocalDate()),
                        lessThan((s1, s2) -> s1.getFin(), Seance::getDebut),
                        greaterThan((s1, s2) -> s2.getDebut(), Seance::getDebut))
                .filter((s1, s2) -> ChronoUnit.MINUTES.between(s1.getFin(),
                        s2.getDebut()) > 120)
                .penalize(HardSoftScore.ONE_SOFT, (s1, s2) -> (int) (ChronoUnit.MINUTES.between(s1.getFin(), s2.getDebut()) - 120))
                .asConstraint(TEACHER_MAX_GAP);
    }

    public Constraint subjectClassMaxSessionsPerDay(ConstraintFactory factory) {
        return factory.forEach(Seance.class)
                .filter(seance -> seance.getClasse() != null && seance.getMatiere() != null && seance.getDebut() != null)
                .groupBy(Seance::getClasse,
                        Seance::getMatiere,
                        seance -> seance.getDebut().toLocalDate(),
                        ConstraintCollectors.count())
                .filter((classe, matiere, date, count) -> count > 1)
                .penalize(HardSoftScore.ONE_SOFT)
                .asConstraint(SUBJECT_CLASS_MAX_SESSIONS_PER_DAY);
    }

    public Constraint subjectClassSpreading(ConstraintFactory factory) {
        return factory.forEach(Seance.class)
                .filter(seance -> seance.getClasse() != null && seance.getMatiere() != null && seance.getDebut() != null)
                .join(Seance.class,
                        equal(Seance::getClasse),
                        equal(Seance::getMatiere),
                        lessThan(Seance::getId))
                .filter((s1, s2) -> {
                    long days = Math.abs(ChronoUnit.DAYS.between(
                            s1.getDebut().toLocalDate(),
                            s2.getDebut().toLocalDate()));
                    return days <= 1;
                })
                .penalize(HardSoftScore.ONE_SOFT)
                .asConstraint(SUBJECT_CLASS_SPREADING);
    }

    public Constraint vieDeClasseTimingConstraint(ConstraintFactory factory) {
        return factory.forEach(Seance.class)
                .filter(seance -> (seance.getType() == Seance.TypeSeance.VIE_DE_CLASSE
                        || (seance.getMatiere() != null && "Vie de classe".equalsIgnoreCase(seance.getMatiere().getNom())))
                        && seance.getDebut() != null && seance.getFin() != null)
                .ifNotExists(ClassePresence.class,
                        equal(Seance::getClasse, ClassePresence::getClasse),
                        filtering((seance, presence) -> presence.isValidVieDeClasse(seance.getDebut(), seance.getFin())))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint(VIE_DE_CLASSE_TIMING);
    }

    public Constraint vieDeClasseReferentConstraint(ConstraintFactory factory) {
        return factory.forEach(Seance.class)
                .filter(seance -> (seance.getType() == Seance.TypeSeance.VIE_DE_CLASSE
                        || (seance.getMatiere() != null && "Vie de classe".equalsIgnoreCase(seance.getMatiere().getNom())))
                        && seance.getClasse() != null
                        && seance.getClasse().getReferents() != null
                        && !seance.getClasse().getReferents().isEmpty()
                        && seance.getProfesseur() != null)
                .filter(seance -> !seance.getClasse().isProfesseurReferent(seance.getProfesseur()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint(VIE_DE_CLASSE_REFERENT);
    }

    public Constraint vieDeClassePrincipalReferentPreference(ConstraintFactory factory) {
        return factory.forEach(Seance.class)
                .filter(seance -> (seance.getType() == Seance.TypeSeance.VIE_DE_CLASSE
                        || (seance.getMatiere() != null && "Vie de classe".equalsIgnoreCase(seance.getMatiere().getNom())))
                        && seance.getClasse() != null
                        && seance.getClasse().getReferents() != null
                        && !seance.getClasse().getReferents().isEmpty()
                        && seance.getProfesseur() != null)
                .filter(seance -> {
                    fr.manaken.plannif.model.Professeur principal = seance.getClasse().getProfesseurReferentPrincipal();
                    return principal != null && principal.getId() != null && !principal.getId().equals(seance.getProfesseur().getId());
                })
                .penalize(HardSoftScore.ONE_SOFT)
                .asConstraint(VIE_DE_CLASSE_PRINCIPAL_PREFERENCE);
    }

    // ==========================================
    // Phase 3 : Salles & Équipements
    // ==========================================

    public Constraint roomCapacity(ConstraintFactory factory) {
        return factory.forEach(Seance.class)
                .filter(seance -> seance.getSalle() != null && seance.getClasse() != null
                && seance.getSalle().getCapacite() != null
                && seance.getClasse().getEleves() != null
                && seance.getClasse().getEleves().size() > seance.getSalle().getCapacite())
                .penalize(HardSoftScore.ONE_HARD, seance -> seance.getClasse().getEleves().size() - seance.getSalle().getCapacite())
                .asConstraint(ROOM_CAPACITY);
    }

    public Constraint roomEquipmentMatch(ConstraintFactory factory) {
        var directStream = factory.forEach(Seance.class)
                .filter(seance -> seance.getSalle() != null && seance.getRequiredRoomType() != null)
                .filter(seance -> seance.getSalle().getType() == null
                        || !seance.getRequiredRoomType().equalsIgnoreCase(seance.getSalle().getType()));

        var fallbackStream = factory.forEach(Seance.class)
                .filter(seance -> seance.getSalle() != null && seance.getRequiredRoomType() == null
                        && seance.getClasse() != null && seance.getMatiere() != null)
                .join(MatiereClasseConfig.class,
                        Joiners.equal(Seance::getClasse, MatiereClasseConfig::getClasse),
                        Joiners.equal(Seance::getMatiere, MatiereClasseConfig::getMatiere))
                .filter((seance, config) -> config.getTypeSalleRequis() != null
                        && (seance.getSalle().getType() == null
                        || !config.getTypeSalleRequis().equalsIgnoreCase(seance.getSalle().getType())))
                .map((seance, config) -> seance);

        return directStream.concat(fallbackStream)
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint(ROOM_EQUIPMENT_MATCH);
    }

    // ==========================================
    // Phase 4 : Rythme Élèves
    // ==========================================

    public Constraint studentLunchBreak(ConstraintFactory factory) {
        return factory.forEach(Seance.class)
                .filter(s -> s.getClasse() != null && s.getDebut() != null && s.getFin() != null)
                .join(Seance.class,
                        equal(Seance::getClasse),
                        equal(s -> s.getDebut().toLocalDate(), s -> s.getDebut().toLocalDate()),
                        lessThan(Seance::getFin, Seance::getDebut))
                .ifNotExists(Seance.class,
                        equal((s1, s2) -> s1.getClasse(), Seance::getClasse),
                        equal((s1, s2) -> s1.getDebut().toLocalDate(), s -> s.getDebut().toLocalDate()),
                        lessThan((s1, s2) -> s1.getFin(), Seance::getDebut),
                        greaterThan((s1, s2) -> s2.getDebut(), Seance::getDebut))
                .filter((s1, s2) -> {
                    LocalTime f1 = s1.getFin().toLocalTime();
                    LocalTime d2 = s2.getDebut().toLocalTime();
                    return f1.isAfter(LocalTime.of(12, 0)) && d2.isBefore(LocalTime.of(13, 30))
                            && ChronoUnit.MINUTES.between(s1.getFin(), s2.getDebut()) < 60;
                })
                .penalize(HardSoftScore.ONE_SOFT, (s1, s2) -> (int) (60 - ChronoUnit.MINUTES.between(s1.getFin(), s2.getDebut())))
                .asConstraint(STUDENT_LUNCH_BREAK);
    }

    public Constraint studentMaxHoursPerDay(ConstraintFactory factory) {
        return factory.forEach(Seance.class)
                .filter(seance -> seance.getClasse() != null && seance.getDebut() != null && seance.getFin() != null)
                .groupBy(Seance::getClasse,
                        seance -> seance.getDebut().toLocalDate(),
                        ConstraintCollectors.sumLong(this::getDurationInMinutes))
                .filter((classe, date, totalMinutes) -> totalMinutes > 420) // max 7h/jour
                .penalize(HardSoftScore.ONE_SOFT, (classe, date, totalMinutes) -> (int) (totalMinutes - 420))
                .asConstraint(STUDENT_MAX_HOURS_PER_DAY);
    }

    public Constraint studentMaxGap(ConstraintFactory factory) {
        return factory.forEach(Seance.class)
                .filter(s -> s.getClasse() != null && s.getDebut() != null && s.getFin() != null)
                .join(Seance.class,
                        equal(Seance::getClasse),
                        equal(s -> s.getFin().toLocalDate(), s -> s.getDebut().toLocalDate()),
                        lessThan(Seance::getFin, Seance::getDebut))
                .ifNotExists(Seance.class,
                        equal((s1, s2) -> s1.getClasse(), Seance::getClasse),
                        equal((s1, s2) -> s1.getFin().toLocalDate(), s -> s.getDebut().toLocalDate()),
                        lessThan((s1, s2) -> s1.getFin(), Seance::getDebut),
                        greaterThan((s1, s2) -> s2.getDebut(), Seance::getDebut))
                .filter((s1, s2) -> {
                    long gap = ChronoUnit.MINUTES.between(s1.getFin(), s2.getDebut());
                    boolean isLunchGap = !s1.getFin().toLocalTime().isBefore(LocalTime.of(11, 0))
                            && !s2.getDebut().toLocalTime().isAfter(LocalTime.of(14, 30))
                            && gap <= 120;
                    return gap > 0 && !isLunchGap;
                })
                .penalize(HardSoftScore.ONE_SOFT, (s1, s2) -> (int) ChronoUnit.MINUTES.between(s1.getFin(), s2.getDebut()))
                .asConstraint(STUDENT_MAX_GAP);
    }

    public Constraint studentDayStartTime(ConstraintFactory factory) {
        return factory.forEach(Seance.class)
                .filter(seance -> seance.getClasse() != null && seance.getDebut() != null)
                .groupBy(Seance::getClasse,
                        seance -> seance.getDebut().toLocalDate(),
                        ConstraintCollectors.min(Seance::getDebut))
                .filter((classe, date, minDebut) -> {
                    LocalTime expectedStart = (date.getDayOfWeek() == DayOfWeek.MONDAY)
                            ? LocalTime.of(9, 0)
                            : LocalTime.of(8, 0);
                    return minDebut.toLocalTime().isAfter(expectedStart);
                })
                .penalize(HardSoftScore.ONE_SOFT, (classe, date, minDebut) -> {
                    LocalTime expectedStart = (date.getDayOfWeek() == DayOfWeek.MONDAY)
                            ? LocalTime.of(9, 0)
                            : LocalTime.of(8, 0);
                    return (int) ChronoUnit.MINUTES.between(expectedStart, minDebut.toLocalTime());
                })
                .asConstraint(STUDENT_DAY_START_TIME);
    }

    public Constraint alignedSessions(ConstraintFactory factory) {
        return factory.forEach(Seance.class)
                .filter(s -> s.getAlignementId() != null && s.getDebut() != null)
                .join(Seance.class,
                        equal(Seance::getAlignementId),
                        lessThan(Seance::getId))
                .filter((s1, s2) -> s2.getDebut() != null && !s1.getDebut().equals(s2.getDebut()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint(ALIGNED_SESSIONS);
    }

    // ==========================================
    // Phase 5 : Enseignants Avancés & Déplacements
    // ==========================================

    public Constraint teacherLunchBreak(ConstraintFactory factory) {
        return factory.forEach(Seance.class)
                .filter(s -> s.getProfesseur() != null && s.getDebut() != null && s.getFin() != null)
                .join(Seance.class,
                        equal(Seance::getProfesseur),
                        equal(s -> s.getDebut().toLocalDate(), s -> s.getDebut().toLocalDate()),
                        lessThan(Seance::getFin, Seance::getDebut))
                .ifNotExists(Seance.class,
                        equal((s1, s2) -> s1.getProfesseur(), Seance::getProfesseur),
                        equal((s1, s2) -> s1.getDebut().toLocalDate(), s -> s.getDebut().toLocalDate()),
                        lessThan((s1, s2) -> s1.getFin(), Seance::getDebut),
                        greaterThan((s1, s2) -> s2.getDebut(), Seance::getDebut))
                .filter((s1, s2) -> {
                    LocalTime f1 = s1.getFin().toLocalTime();
                    LocalTime d2 = s2.getDebut().toLocalTime();
                    return f1.isAfter(LocalTime.of(12, 0)) && d2.isBefore(LocalTime.of(13, 30))
                            && ChronoUnit.MINUTES.between(s1.getFin(), s2.getDebut()) < 45;
                })
                .penalize(HardSoftScore.ONE_SOFT, (s1, s2) -> (int) (45 - ChronoUnit.MINUTES.between(s1.getFin(), s2.getDebut())))
                .asConstraint(TEACHER_LUNCH_BREAK);
    }

    public Constraint teacherUnavailability(ConstraintFactory factory) {
        return factory.forEach(Seance.class)
                .filter(s -> s.getProfesseur() != null && s.getDebut() != null && s.getFin() != null)
                .join(ProfesseurIndisponibilite.class,
                        equal(Seance::getProfesseur, ProfesseurIndisponibilite::getProfesseur))
                .filter((seance, indispo) -> indispo.matches(seance.getDebut(), seance.getFin()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint(TEACHER_UNAVAILABILITY);
    }

    public Constraint teacherTravelDistance(ConstraintFactory factory) {
        var distantRooms = factory.forEach(DistanceSalle.class)
                .filter(d -> d.getDistance() != null && d.getDistance() > 200 && d.getSalle1() != null && d.getSalle2() != null);

        return factory.forEach(Seance.class)
                .filter(s -> s.getProfesseur() != null && s.getSalle() != null && s.getDebut() != null && s.getFin() != null)
                .join(Seance.class,
                        equal(Seance::getProfesseur),
                        equal(s -> s.getFin().toLocalDate(), s -> s.getDebut().toLocalDate()),
                        equal(Seance::getFin, Seance::getDebut))
                .filter((s1, s2) -> !s1.getSalle().equals(s2.getSalle()))
                .join(distantRooms,
                        filtering((s1, s2, dist) ->
                                (dist.getSalle1().equals(s1.getSalle()) && dist.getSalle2().equals(s2.getSalle())) ||
                                (dist.getSalle1().equals(s2.getSalle()) && dist.getSalle2().equals(s1.getSalle()))
                        ))
                .penalize(HardSoftScore.ONE_SOFT)
                .asConstraint(TEACHER_TRAVEL_DISTANCE);
    }

    // ==========================================
    // Phase 6 : Organisation Pédagogique
    // ==========================================

    public Constraint highCognitiveLoad(ConstraintFactory factory) {
        return factory.forEach(Seance.class)
                .filter(seance -> seance.getDebut() != null && seance.getClasse() != null && seance.getMatiere() != null)
                .join(MatiereClasseConfig.class,
                        Joiners.equal(Seance::getClasse, MatiereClasseConfig::getClasse),
                        Joiners.equal(Seance::getMatiere, MatiereClasseConfig::getMatiere))
                .filter((seance, config) -> Boolean.TRUE.equals(config.getPrioriteMatin())
                        && seance.getDebut().toLocalTime().isAfter(LocalTime.of(12, 30)))
                .penalize(HardSoftScore.ONE_SOFT)
                .asConstraint(HIGH_COGNITIVE_LOAD);
    }

    public Constraint sessionPairing(ConstraintFactory factory) {
        return factory.forEach(Seance.class)
                .filter(s -> s.getClasse() != null && s.getMatiere() != null && s.getDebut() != null && s.getFin() != null)
                .join(Seance.class,
                        equal(Seance::getClasse),
                        equal(Seance::getMatiere),
                        equal(s -> s.getDebut().toLocalDate(), s -> s.getDebut().toLocalDate()),
                        lessThan(Seance::getId))
                .filter((s1, s2) -> s1.getActualDurationMinutes() == 60 && s2.getActualDurationMinutes() == 60
                        && !s1.getFin().equals(s2.getDebut()) && !s2.getFin().equals(s1.getDebut()))
                .penalize(HardSoftScore.ONE_SOFT)
                .asConstraint(SESSION_PAIRING);
    }
}
