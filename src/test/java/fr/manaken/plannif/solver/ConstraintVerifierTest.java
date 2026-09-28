package fr.manaken.plannif.solver;

import ai.timefold.solver.test.api.score.stream.ConstraintVerifier;
import fr.manaken.plannif.business.Planning;
import fr.manaken.plannif.business.PlanningConstraints;
import fr.manaken.plannif.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class ConstraintVerifierTest {

    ConstraintVerifier<PlanningConstraints, Planning> constraintVerifier = ConstraintVerifier.build(
            new PlanningConstraints(), Planning.class, Seance.class);

    @Test
    void roomConflict() {
        Planning problem = new Planning();
        LocalDateTime debut = LocalDateTime.of(2024, 2, 12, 8, 0);

        Salle salle = new Salle();
        salle.setId(1L);

        Professeur prof1 = new Professeur();
        prof1.setId(1L);
        Professeur prof2 = new Professeur();
        prof2.setId(2L);

        Seance s1 = new Seance();
        s1.setId(1L);
        s1.setSalle(salle);
        s1.setDebut(debut);
        s1.setProfesseur(prof1);

        Seance s2 = new Seance();
        s2.setId(2L);
        s2.setSalle(salle);
        s2.setDebut(debut);
        s2.setProfesseur(prof2);

        problem.getSeances().addAll(java.util.List.of(s1, s2));
        problem.getSalles().add(salle);
        problem.getDatesDebutPossibles().add(debut);
        problem.getProfesseurs().addAll(java.util.List.of(prof1, prof2));

        constraintVerifier.verifyThat(PlanningConstraints::roomConflict)
                .givenSolution(problem)
                .penalizesBy(1);
    }

    @Test
    void teacherConflict() {
        Planning problem = new Planning();
        LocalDateTime debut = LocalDateTime.of(2024, 2, 12, 8, 0);

        Salle salle = new Salle();
        salle.setId(1L);

        Professeur prof = new Professeur();
        prof.setId(1L);

        Seance s1 = new Seance();
        s1.setId(1L);
        s1.setProfesseur(prof);
        s1.setDebut(debut);
        s1.setSalle(salle);

        Seance s2 = new Seance();
        s2.setId(2L);
        s2.setProfesseur(prof);
        s2.setDebut(debut);
        s2.setSalle(salle);

        problem.getSeances().addAll(java.util.List.of(s1, s2));
        problem.getDatesDebutPossibles().add(debut);
        problem.getSalles().add(salle);
        problem.getProfesseurs().add(prof);

        constraintVerifier.verifyThat(PlanningConstraints::teacherConflict)
                .givenSolution(problem)
                .penalizesBy(1);
    }

    @Test
    void studentGroupConflict() {
        Planning problem = new Planning();
        LocalDateTime debut = LocalDateTime.of(2024, 2, 12, 8, 0);

        Salle salle = new Salle();
        salle.setId(1L);

        Classe classe = new Classe();
        classe.setId(1L);

        Professeur prof1 = new Professeur();
        prof1.setId(1L);
        Professeur prof2 = new Professeur();
        prof2.setId(2L);

        Seance s1 = new Seance();
        s1.setId(1L);
        s1.setClasse(classe);
        s1.setDebut(debut);
        s1.setSalle(salle);
        s1.setProfesseur(prof1);

        Seance s2 = new Seance();
        s2.setId(2L);
        s2.setClasse(classe);
        s2.setDebut(debut);
        s2.setSalle(salle);
        s2.setProfesseur(prof2);

        problem.getSeances().addAll(java.util.List.of(s1, s2));
        problem.getDatesDebutPossibles().add(debut);
        problem.getSalles().add(salle);
        problem.getClasses().add(classe);
        problem.getProfesseurs().addAll(java.util.List.of(prof1, prof2));

        constraintVerifier.verifyThat(PlanningConstraints::studentGroupConflict)
                .givenSolution(problem)
                .penalizesBy(1);
    }

    @Test
    void teacherDayOff() {
        Planning problem = new Planning();
        LocalDateTime mondayDebut = LocalDateTime.of(2024, 2, 12, 8, 0); // 2024-02-12 is Monday

        Salle salle = new Salle();
        salle.setId(1L);

        Professeur prof = new Professeur();
        prof.setId(1L);

        ProfesseurDayOff dayOff = new ProfesseurDayOff();
        dayOff.setId(1L);
        dayOff.setProfesseur(prof);
        dayOff.setDayOfWeek(0); // Monday

        Seance s1 = new Seance();
        s1.setId(1L);
        s1.setProfesseur(prof);
        s1.setDebut(mondayDebut);
        s1.setSalle(salle);

        problem.getSeances().add(s1);
        problem.getDatesDebutPossibles().add(mondayDebut);
        problem.getSalles().add(salle);
        problem.getProfesseurs().add(prof);
        problem.getProfesseurDayOffs().add(dayOff);

        constraintVerifier.verifyThat(PlanningConstraints::teacherDayOff)
                .givenSolution(problem)
                .penalizesBy(1);
    }

    @Test
    void teacherMaxHoursPerDay() {
        Planning problem = new Planning();
        LocalDateTime d1 = LocalDateTime.of(2024, 2, 12, 8, 0);
        LocalDateTime f1 = LocalDateTime.of(2024, 2, 12, 12, 0);

        LocalDateTime d2 = LocalDateTime.of(2024, 2, 12, 14, 0);
        LocalDateTime f2 = LocalDateTime.of(2024, 2, 12, 18, 0);

        Salle salle = new Salle();
        salle.setId(1L);

        Professeur prof = new Professeur();
        prof.setId(1L);
        prof.setMaxHeuresParJour(BigDecimal.valueOf(6));

        Seance s1 = new Seance();
        s1.setId(1L);
        s1.setProfesseur(prof);
        s1.setDebut(d1);
        s1.setFin(f1);
        s1.setSalle(salle);

        Seance s2 = new Seance();
        s2.setId(2L);
        s2.setProfesseur(prof);
        s2.setDebut(d2);
        s2.setFin(f2);
        s2.setSalle(salle);

        problem.getSeances().addAll(java.util.List.of(s1, s2));
        problem.getDatesDebutPossibles().addAll(java.util.List.of(d1, d2));
        problem.getSalles().add(salle);
        problem.getProfesseurs().add(prof);

        constraintVerifier.verifyThat(PlanningConstraints::teacherMaxHoursPerDay)
                .givenSolution(problem)
                .penalizesBy(1);
    }

    @Test
    void teacherMaxHoursPerWeek() {
        Planning problem = new Planning();
        LocalDateTime d1 = LocalDateTime.of(2024, 2, 12, 8, 0);
        LocalDateTime f1 = LocalDateTime.of(2024, 2, 12, 18, 0);

        LocalDateTime d2 = LocalDateTime.of(2024, 2, 13, 8, 0); // Same week (ISO week 7)
        LocalDateTime f2 = LocalDateTime.of(2024, 2, 13, 18, 0);

        Salle salle = new Salle();
        salle.setId(1L);

        Professeur prof = new Professeur();
        prof.setId(1L);
        prof.setMaxHeuresParSemaine(BigDecimal.valueOf(15));

        Seance s1 = new Seance();
        s1.setId(1L);
        s1.setProfesseur(prof);
        s1.setDebut(d1);
        s1.setFin(f1);
        s1.setSalle(salle);

        Seance s2 = new Seance();
        s2.setId(2L);
        s2.setProfesseur(prof);
        s2.setDebut(d2);
        s2.setFin(f2);
        s2.setSalle(salle);

        problem.getSeances().addAll(java.util.List.of(s1, s2));
        problem.getDatesDebutPossibles().addAll(java.util.List.of(d1, d2));
        problem.getSalles().add(salle);
        problem.getProfesseurs().add(prof);

        constraintVerifier.verifyThat(PlanningConstraints::teacherMaxHoursPerWeek)
                .givenSolution(problem)
                .penalizesBy(1);
    }

    @Test
    void teacherMaxHoursPerSession() {
        Planning problem = new Planning();
        LocalDateTime d1 = LocalDateTime.of(2024, 2, 12, 8, 0);
        LocalDateTime f1 = LocalDateTime.of(2024, 2, 12, 12, 0);

        Salle salle = new Salle();
        salle.setId(1L);

        Professeur prof = new Professeur();
        prof.setId(1L);
        prof.setMaxHeuresParSeance(BigDecimal.valueOf(3));

        Seance s1 = new Seance();
        s1.setId(1L);
        s1.setProfesseur(prof);
        s1.setDebut(d1);
        s1.setFin(f1);
        s1.setSalle(salle);

        problem.getSeances().add(s1);
        problem.getDatesDebutPossibles().add(d1);
        problem.getSalles().add(salle);
        problem.getProfesseurs().add(prof);

        constraintVerifier.verifyThat(PlanningConstraints::teacherMaxHoursPerSession)
                .givenSolution(problem)
                .penalizesBy(1);
    }

    @Test
    void teacherClassMaxHoursConsecutive() {
        Planning problem = new Planning();
        LocalDateTime d1 = LocalDateTime.of(2024, 2, 12, 8, 0);
        LocalDateTime f1 = LocalDateTime.of(2024, 2, 12, 11, 0);

        LocalDateTime d2 = LocalDateTime.of(2024, 2, 13, 8, 0);
        LocalDateTime f2 = LocalDateTime.of(2024, 2, 13, 11, 0);

        Salle salle = new Salle();
        salle.setId(1L);

        Professeur prof = new Professeur();
        prof.setId(1L);
        Classe classe = new Classe();
        classe.setId(1L);

        Seance s1 = new Seance();
        s1.setId(1L);
        s1.setProfesseur(prof);
        s1.setClasse(classe);
        s1.setDebut(d1);
        s1.setFin(f1);
        s1.setSalle(salle);

        Seance s2 = new Seance();
        s2.setId(2L);
        s2.setProfesseur(prof);
        s2.setClasse(classe);
        s2.setDebut(d2);
        s2.setFin(f2);
        s2.setSalle(salle);

        problem.getSeances().addAll(java.util.List.of(s1, s2));
        problem.getDatesDebutPossibles().addAll(java.util.List.of(d1, d2));
        problem.getSalles().add(salle);
        problem.getProfesseurs().add(prof);
        problem.getClasses().add(classe);

        constraintVerifier.verifyThat(PlanningConstraints::teacherClassMaxHoursConsecutive)
                .givenSolution(problem)
                .penalizesBy(1);
    }

    @Test
    void studentGroupPresence() {
        Planning problem = new Planning();
        LocalDateTime debut = LocalDateTime.of(2024, 2, 12, 8, 0);

        Salle salle = new Salle();
        salle.setId(1L);

        Classe classe = new Classe();
        classe.setId(1L);

        Professeur prof = new Professeur();
        prof.setId(1L);

        Seance s1 = new Seance();
        s1.setId(1L);
        s1.setClasse(classe);
        s1.setDebut(debut);
        s1.setSalle(salle);
        s1.setProfesseur(prof);

        problem.getSeances().add(s1);
        problem.getDatesDebutPossibles().add(debut);
        problem.getSalles().add(salle);
        problem.getClasses().add(classe);
        problem.getProfesseurs().add(prof);

        // No presence for this class
        constraintVerifier.verifyThat(PlanningConstraints::studentGroupPresence)
                .givenSolution(problem)
                .penalizesBy(1);

        // Presence exists covering the date 2024-02-12
        ClassePresence presence = new ClassePresence();
        presence.setId(1L);
        presence.setClasse(classe);
        presence.setDateDebut(LocalDate.of(2024, 2, 12));
        presence.setDateFin(LocalDate.of(2024, 2, 12));

        problem.getClassePresences().add(presence);

        constraintVerifier.verifyThat(PlanningConstraints::studentGroupPresence)
                .givenSolution(problem)
                .penalizesBy(0);
    }

    @Test
    void vieDeClasseTimingConstraint() {
        Planning problem = new Planning();
        
        Classe classe = new Classe();
        classe.setId(1L);

        ClassePresence presence = new ClassePresence();
        presence.setId(1L);
        presence.setClasse(classe);
        presence.setDateDebut(LocalDate.of(2024, 2, 12)); // 2024-02-12 is Monday
        presence.setDateFin(LocalDate.of(2024, 2, 25)); // last Friday is 2024-02-23

        // Valid Monday slot: first Monday (2024-02-12) 9h-10h
        LocalDateTime d_monday_ok = LocalDateTime.of(2024, 2, 12, 9, 0);
        LocalDateTime f_monday_ok = LocalDateTime.of(2024, 2, 12, 10, 0);

        // Valid Friday slot: last Friday (2024-02-23) 10h-11h
        LocalDateTime d_friday_ok = LocalDateTime.of(2024, 2, 23, 10, 0);
        LocalDateTime f_friday_ok = LocalDateTime.of(2024, 2, 23, 11, 0);

        // Invalid Monday slot (wrong time): first Monday 10h-11h
        LocalDateTime d_monday_wrong_time = LocalDateTime.of(2024, 2, 12, 10, 0);
        LocalDateTime f_monday_wrong_time = LocalDateTime.of(2024, 2, 12, 11, 0);

        // Invalid Friday slot (wrong time): last Friday 9h-10h
        LocalDateTime d_friday_wrong_time = LocalDateTime.of(2024, 2, 23, 9, 0);
        LocalDateTime f_friday_wrong_time = LocalDateTime.of(2024, 2, 23, 10, 0);

        // Invalid slot (wrong date): middle Friday (2024-02-16) 10h-11h
        LocalDateTime d_wrong_date = LocalDateTime.of(2024, 2, 16, 10, 0);
        LocalDateTime f_wrong_date = LocalDateTime.of(2024, 2, 16, 11, 0);

        Salle salle = new Salle();
        salle.setId(1L);

        Professeur prof = new Professeur();
        prof.setId(1L);

        Seance s_monday_ok = new Seance();
        s_monday_ok.setId(1L);
        s_monday_ok.setClasse(classe);
        s_monday_ok.setType(Seance.TypeSeance.VIE_DE_CLASSE);
        s_monday_ok.setDebut(d_monday_ok);
        s_monday_ok.setFin(f_monday_ok);
        s_monday_ok.setSalle(salle);
        s_monday_ok.setProfesseur(prof);

        Seance s_friday_ok = new Seance();
        s_friday_ok.setId(2L);
        s_friday_ok.setClasse(classe);
        s_friday_ok.setType(Seance.TypeSeance.VIE_DE_CLASSE);
        s_friday_ok.setDebut(d_friday_ok);
        s_friday_ok.setFin(f_friday_ok);
        s_friday_ok.setSalle(salle);
        s_friday_ok.setProfesseur(prof);

        Seance s_monday_wrong_time = new Seance();
        s_monday_wrong_time.setId(3L);
        s_monday_wrong_time.setClasse(classe);
        s_monday_wrong_time.setType(Seance.TypeSeance.VIE_DE_CLASSE);
        s_monday_wrong_time.setDebut(d_monday_wrong_time);
        s_monday_wrong_time.setFin(f_monday_wrong_time);
        s_monday_wrong_time.setSalle(salle);
        s_monday_wrong_time.setProfesseur(prof);

        Seance s_friday_wrong_time = new Seance();
        s_friday_wrong_time.setId(4L);
        s_friday_wrong_time.setClasse(classe);
        s_friday_wrong_time.setType(Seance.TypeSeance.VIE_DE_CLASSE);
        s_friday_wrong_time.setDebut(d_friday_wrong_time);
        s_friday_wrong_time.setFin(f_friday_wrong_time);
        s_friday_wrong_time.setSalle(salle);
        s_friday_wrong_time.setProfesseur(prof);

        Seance s_wrong_date = new Seance();
        s_wrong_date.setId(5L);
        s_wrong_date.setClasse(classe);
        s_wrong_date.setType(Seance.TypeSeance.VIE_DE_CLASSE);
        s_wrong_date.setDebut(d_wrong_date);
        s_wrong_date.setFin(f_wrong_date);
        s_wrong_date.setSalle(salle);
        s_wrong_date.setProfesseur(prof);

        problem.getSeances().addAll(java.util.List.of(s_monday_ok, s_friday_ok, s_monday_wrong_time, s_friday_wrong_time, s_wrong_date));
        problem.getDatesDebutPossibles().addAll(java.util.List.of(d_monday_ok, d_friday_ok, d_monday_wrong_time, d_friday_wrong_time, d_wrong_date));
        problem.getSalles().add(salle);
        problem.getProfesseurs().add(prof);
        problem.getClasses().add(classe);
        problem.getClassePresences().add(presence);

        constraintVerifier.verifyThat(PlanningConstraints::vieDeClasseTimingConstraint)
                .givenSolution(problem)
                .penalizesBy(3);
    }

    @Test
    void vieDeClasseTimingConstraint_MondayAndFridayTogether() {
        Planning problem = new Planning();

        Classe classe = new Classe();
        classe.setId(1L);

        ClassePresence presence = new ClassePresence();
        presence.setId(1L);
        presence.setClasse(classe);
        presence.setDateDebut(LocalDate.of(2024, 2, 12)); // Monday
        presence.setDateFin(LocalDate.of(2024, 2, 23));   // Friday

        LocalDateTime d_monday = LocalDateTime.of(2024, 2, 12, 9, 0);
        LocalDateTime f_monday = LocalDateTime.of(2024, 2, 12, 10, 0);

        LocalDateTime d_friday = LocalDateTime.of(2024, 2, 23, 10, 0);
        LocalDateTime f_friday = LocalDateTime.of(2024, 2, 23, 11, 0);

        Salle salle1 = new Salle();
        salle1.setId(1L);

        Salle salle2 = new Salle();
        salle2.setId(2L);

        Professeur prof1 = new Professeur();
        prof1.setId(1L);

        Professeur prof2 = new Professeur();
        prof2.setId(2L);

        Seance s_monday = new Seance();
        s_monday.setId(1L);
        s_monday.setClasse(classe);
        s_monday.setType(Seance.TypeSeance.VIE_DE_CLASSE);
        s_monday.setDebut(d_monday);
        s_monday.setFin(f_monday);
        s_monday.setSalle(salle1);
        s_monday.setProfesseur(prof1);

        Seance s_friday = new Seance();
        s_friday.setId(2L);
        s_friday.setClasse(classe);
        s_friday.setType(Seance.TypeSeance.VIE_DE_CLASSE);
        s_friday.setDebut(d_friday);
        s_friday.setFin(f_friday);
        s_friday.setSalle(salle2);
        s_friday.setProfesseur(prof2);

        problem.getSeances().addAll(java.util.List.of(s_monday, s_friday));
        problem.getDatesDebutPossibles().addAll(java.util.List.of(d_monday, d_friday));
        problem.getSalles().addAll(java.util.List.of(salle1, salle2));
        problem.getProfesseurs().addAll(java.util.List.of(prof1, prof2));
        problem.getClasses().add(classe);
        problem.getClassePresences().add(presence);

        constraintVerifier.verifyThat(PlanningConstraints::vieDeClasseTimingConstraint)
                .givenSolution(problem)
                .penalizesBy(0);
    }

    @Test
    void vieDeClasseTimingConstraint_FirstAndLastDaysNonMondayFriday() {
        Planning problem = new Planning();

        Classe classe = new Classe();
        classe.setId(1L);

        // Period from Tuesday 2024-02-13 to Thursday 2024-02-22
        ClassePresence presence = new ClassePresence();
        presence.setId(1L);
        presence.setClasse(classe);
        presence.setDateDebut(LocalDate.of(2024, 2, 13)); // Tuesday (first day)
        presence.setDateFin(LocalDate.of(2024, 2, 22));   // Thursday (last day)

        // Valid: Tuesday (first day) 9h-10h
        LocalDateTime d_tuesday = LocalDateTime.of(2024, 2, 13, 9, 0);
        LocalDateTime f_tuesday = LocalDateTime.of(2024, 2, 13, 10, 0);

        // Valid: Thursday (last day) 10h-11h
        LocalDateTime d_thursday = LocalDateTime.of(2024, 2, 22, 10, 0);
        LocalDateTime f_thursday = LocalDateTime.of(2024, 2, 22, 11, 0);

        // Invalid: Monday 2024-02-19 9h-10h (Monday is NOT the first day)
        LocalDateTime d_monday_invalid = LocalDateTime.of(2024, 2, 19, 9, 0);
        LocalDateTime f_monday_invalid = LocalDateTime.of(2024, 2, 19, 10, 0);

        // Invalid: Friday 2024-02-16 10h-11h (Friday is NOT the last day)
        LocalDateTime d_friday_invalid = LocalDateTime.of(2024, 2, 16, 10, 0);
        LocalDateTime f_friday_invalid = LocalDateTime.of(2024, 2, 16, 11, 0);

        Salle salle = new Salle();
        salle.setId(1L);

        Professeur prof = new Professeur();
        prof.setId(1L);

        Seance s1_valid = new Seance();
        s1_valid.setId(1L);
        s1_valid.setClasse(classe);
        s1_valid.setType(Seance.TypeSeance.VIE_DE_CLASSE);
        s1_valid.setDebut(d_tuesday);
        s1_valid.setFin(f_tuesday);
        s1_valid.setSalle(salle);
        s1_valid.setProfesseur(prof);

        Seance s2_valid = new Seance();
        s2_valid.setId(2L);
        s2_valid.setClasse(classe);
        s2_valid.setType(Seance.TypeSeance.VIE_DE_CLASSE);
        s2_valid.setDebut(d_thursday);
        s2_valid.setFin(f_thursday);
        s2_valid.setSalle(salle);
        s2_valid.setProfesseur(prof);

        Seance s3_invalid = new Seance();
        s3_invalid.setId(3L);
        s3_invalid.setClasse(classe);
        s3_invalid.setType(Seance.TypeSeance.VIE_DE_CLASSE);
        s3_invalid.setDebut(d_monday_invalid);
        s3_invalid.setFin(f_monday_invalid);
        s3_invalid.setSalle(salle);
        s3_invalid.setProfesseur(prof);

        Seance s4_invalid = new Seance();
        s4_invalid.setId(4L);
        s4_invalid.setClasse(classe);
        s4_invalid.setType(Seance.TypeSeance.VIE_DE_CLASSE);
        s4_invalid.setDebut(d_friday_invalid);
        s4_invalid.setFin(f_friday_invalid);
        s4_invalid.setSalle(salle);
        s4_invalid.setProfesseur(prof);

        problem.getSeances().addAll(java.util.List.of(s1_valid, s2_valid, s3_invalid, s4_invalid));
        problem.getDatesDebutPossibles().addAll(java.util.List.of(d_tuesday, d_thursday, d_monday_invalid, d_friday_invalid));
        problem.getSalles().add(salle);
        problem.getProfesseurs().add(prof);
        problem.getClasses().add(classe);
        problem.getClassePresences().add(presence);

        // s1 and s2 should not be penalized, s3 and s4 should each be penalized 1 HARD
        constraintVerifier.verifyThat(PlanningConstraints::vieDeClasseTimingConstraint)
                .givenSolution(problem)
                .penalizesBy(2);
    }

    @Test
    void seanceTypeDurationMatch() {
        Planning problem = new Planning();

        LocalDateTime d1 = LocalDateTime.of(2024, 2, 12, 8, 0);
        LocalDateTime f1 = LocalDateTime.of(2024, 2, 12, 9, 30); // 1h30 (90 mins)

        LocalDateTime d2 = LocalDateTime.of(2024, 2, 12, 10, 0);
        LocalDateTime f2 = LocalDateTime.of(2024, 2, 12, 11, 0); // 1h (60 mins)

        Salle salle = new Salle();
        salle.setId(1L);

        Professeur prof = new Professeur();
        prof.setId(1L);

        // Seance 1: TP on a 1.5h slot (matching) -> no penalty
        Seance s1 = new Seance();
        s1.setId(1L);
        s1.setType(Seance.TypeSeance.TP);
        s1.setDebut(d1);
        s1.setFin(f1);
        s1.setSalle(salle);
        s1.setProfesseur(prof);

        // Seance 2: COURS on a 1.5h slot (mismatching) -> 1 penalty
        Seance s2 = new Seance();
        s2.setId(2L);
        s2.setType(Seance.TypeSeance.COURS);
        s2.setDebut(d1);
        s2.setFin(f1);
        s2.setSalle(salle);
        s2.setProfesseur(prof);

        // Seance 3: TP on a 1h slot (mismatching) -> 1 penalty
        Seance s3 = new Seance();
        s3.setId(3L);
        s3.setType(Seance.TypeSeance.TP);
        s3.setDebut(d2);
        s3.setFin(f2);
        s3.setSalle(salle);
        s3.setProfesseur(prof);

        // Seance 4: COURS on a 1h slot (matching) -> no penalty
        Seance s4 = new Seance();
        s4.setId(4L);
        s4.setType(Seance.TypeSeance.COURS);
        s4.setDebut(d2);
        s4.setFin(f2);
        s4.setSalle(salle);
        s4.setProfesseur(prof);

        problem.getSeances().addAll(java.util.List.of(s1, s2, s3, s4));
        problem.getDatesDebutPossibles().addAll(java.util.List.of(d1, d2));
        problem.getSalles().add(salle);
        problem.getProfesseurs().add(prof);

        constraintVerifier.verifyThat(PlanningConstraints::seanceTypeDurationMatch)
                .givenSolution(problem)
                .penalizesBy(2);
    }

    @Test
    void vieDeClasseTimingConstraint_SkipHolidaysAndMissingCreneaux() {
        Planning problem = new Planning();

        Classe classe = new Classe();
        classe.setId(1L);

        ClassePresence presence = new ClassePresence();
        presence.setId(1L);
        presence.setClasse(classe);
        presence.setDateDebut(LocalDate.of(2026, 11, 2));  // Monday, but inside Toussaint holidays
        presence.setDateFin(LocalDate.of(2026, 11, 22));  // Sunday

        Vacances toussaint = new Vacances();
        toussaint.setId(1L);
        toussaint.setDateDebut(LocalDate.of(2026, 10, 26));
        toussaint.setDateFin(LocalDate.of(2026, 11, 8));

        presence.setVacances(List.of(toussaint));

        // First day should skip holidays (2026-11-02 to 2026-11-08) and be Monday 2026-11-09
        assertThat(presence.getFirstDay()).isEqualTo(LocalDate.of(2026, 11, 9));
        // Last day should skip weekend (2026-11-22 Sunday, 2026-11-21 Saturday) and be Friday 2026-11-20
        assertThat(presence.getLastDay()).isEqualTo(LocalDate.of(2026, 11, 20));

        // Valid session: Monday 2026-11-09 9h-10h
        LocalDateTime d_valid_first = LocalDateTime.of(2026, 11, 9, 9, 0);
        LocalDateTime f_valid_first = LocalDateTime.of(2026, 11, 9, 10, 0);

        // Valid session: Friday 2026-11-20 10h-11h
        LocalDateTime d_valid_last = LocalDateTime.of(2026, 11, 20, 10, 0);
        LocalDateTime f_valid_last = LocalDateTime.of(2026, 11, 20, 11, 0);

        // Invalid session: Monday 2026-11-02 9h-10h (during vacation)
        LocalDateTime d_invalid_holiday = LocalDateTime.of(2026, 11, 2, 9, 0);
        LocalDateTime f_invalid_holiday = LocalDateTime.of(2026, 11, 2, 10, 0);

        Salle salle = new Salle();
        salle.setId(1L);

        Professeur prof = new Professeur();
        prof.setId(1L);

        Seance s1 = new Seance();
        s1.setId(1L);
        s1.setClasse(classe);
        s1.setType(Seance.TypeSeance.VIE_DE_CLASSE);
        s1.setDebut(d_valid_first);
        s1.setFin(f_valid_first);
        s1.setSalle(salle);
        s1.setProfesseur(prof);

        Seance s2 = new Seance();
        s2.setId(2L);
        s2.setClasse(classe);
        s2.setType(Seance.TypeSeance.VIE_DE_CLASSE);
        s2.setDebut(d_valid_last);
        s2.setFin(f_valid_last);
        s2.setSalle(salle);
        s2.setProfesseur(prof);

        Seance s3 = new Seance();
        s3.setId(3L);
        s3.setClasse(classe);
        s3.setType(Seance.TypeSeance.VIE_DE_CLASSE);
        s3.setDebut(d_invalid_holiday);
        s3.setFin(f_invalid_holiday);
        s3.setSalle(salle);
        s3.setProfesseur(prof);

        problem.getSeances().addAll(List.of(s1, s2, s3));
        problem.getDatesDebutPossibles().addAll(List.of(d_valid_first, d_valid_last, d_invalid_holiday));
        problem.getSalles().add(salle);
        problem.getProfesseurs().add(prof);
        problem.getClasses().add(classe);
        problem.getClassePresences().add(presence);
        problem.getVacances().add(toussaint);

        constraintVerifier.verifyThat(PlanningConstraints::vieDeClasseTimingConstraint)
                .givenSolution(problem)
                .penalizesBy(1);
    }
}
