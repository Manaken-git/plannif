package fr.manaken.plannif.solver;

import ai.timefold.solver.test.api.score.stream.ConstraintVerifier;
import fr.manaken.plannif.business.Planning;
import fr.manaken.plannif.business.PlanningConstraints;
import fr.manaken.plannif.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

class ConstraintVerifierTest {

    ConstraintVerifier<PlanningConstraints, Planning> constraintVerifier = ConstraintVerifier.build(
            new PlanningConstraints(), Planning.class, Seance.class);

    @Test
    void roomConflict() {
        Planning problem = new Planning();
        Creneau creneau = new Creneau();
        creneau.setId(1L);
        creneau.setDebut(LocalDateTime.of(2024, 2, 12, 8, 0));
        creneau.setFin(LocalDateTime.of(2024, 2, 12, 9, 0));

        Salle salle = new Salle();
        salle.setId(1L);

        Professeur prof1 = new Professeur();
        prof1.setId(1L);
        Professeur prof2 = new Professeur();
        prof2.setId(2L);

        Seance s1 = new Seance();
        s1.setId(1L);
        s1.setSalle(salle);
        s1.setCreneau(creneau);
        s1.setProfesseur(prof1);

        Seance s2 = new Seance();
        s2.setId(2L);
        s2.setSalle(salle);
        s2.setCreneau(creneau);
        s2.setProfesseur(prof2);

        problem.getSeances().addAll(java.util.List.of(s1, s2));
        problem.getSalles().add(salle);
        problem.getCreneaux().add(creneau);
        problem.getProfesseurs().addAll(java.util.List.of(prof1, prof2));

        constraintVerifier.verifyThat(PlanningConstraints::roomConflict)
                .givenSolution(problem)
                .penalizesBy(1);
    }

    @Test
    void teacherConflict() {
        Planning problem = new Planning();
        Creneau creneau = new Creneau();
        creneau.setId(1L);
        creneau.setDebut(LocalDateTime.of(2024, 2, 12, 8, 0));
        creneau.setFin(LocalDateTime.of(2024, 2, 12, 9, 0));

        Salle salle = new Salle();
        salle.setId(1L);

        Professeur prof = new Professeur();
        prof.setId(1L);

        Seance s1 = new Seance();
        s1.setId(1L);
        s1.setProfesseur(prof);
        s1.setCreneau(creneau);
        s1.setSalle(salle);

        Seance s2 = new Seance();
        s2.setId(2L);
        s2.setProfesseur(prof);
        s2.setCreneau(creneau);
        s2.setSalle(salle);

        problem.getSeances().addAll(java.util.List.of(s1, s2));
        problem.getCreneaux().add(creneau);
        problem.getSalles().add(salle);
        problem.getProfesseurs().add(prof);

        constraintVerifier.verifyThat(PlanningConstraints::teacherConflict)
                .givenSolution(problem)
                .penalizesBy(1);
    }

    @Test
    void studentGroupConflict() {
        Planning problem = new Planning();
        Creneau creneau = new Creneau();
        creneau.setId(1L);
        creneau.setDebut(LocalDateTime.of(2024, 2, 12, 8, 0));
        creneau.setFin(LocalDateTime.of(2024, 2, 12, 9, 0));

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
        s1.setCreneau(creneau);
        s1.setSalle(salle);
        s1.setProfesseur(prof1);

        Seance s2 = new Seance();
        s2.setId(2L);
        s2.setClasse(classe);
        s2.setCreneau(creneau);
        s2.setSalle(salle);
        s2.setProfesseur(prof2);

        problem.getSeances().addAll(java.util.List.of(s1, s2));
        problem.getCreneaux().add(creneau);
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
        Creneau mondayCreneau = new Creneau();
        mondayCreneau.setId(1L);
        mondayCreneau.setDebut(LocalDateTime.of(2024, 2, 12, 8, 0)); // 2024-02-12 is Monday
        mondayCreneau.setFin(LocalDateTime.of(2024, 2, 12, 9, 0));

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
        s1.setCreneau(mondayCreneau);
        s1.setSalle(salle);

        problem.getSeances().add(s1);
        problem.getCreneaux().add(mondayCreneau);
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
        Creneau c1 = new Creneau();
        c1.setId(1L);
        c1.setDebut(LocalDateTime.of(2024, 2, 12, 8, 0));
        c1.setFin(LocalDateTime.of(2024, 2, 12, 12, 0));

        Creneau c2 = new Creneau();
        c2.setId(2L);
        c2.setDebut(LocalDateTime.of(2024, 2, 12, 14, 0));
        c2.setFin(LocalDateTime.of(2024, 2, 12, 18, 0));

        Salle salle = new Salle();
        salle.setId(1L);

        Professeur prof = new Professeur();
        prof.setId(1L);
        prof.setMaxHeuresParJour(BigDecimal.valueOf(6));

        Seance s1 = new Seance();
        s1.setId(1L);
        s1.setProfesseur(prof);
        s1.setCreneau(c1);
        s1.setSalle(salle);

        Seance s2 = new Seance();
        s2.setId(2L);
        s2.setProfesseur(prof);
        s2.setCreneau(c2);
        s2.setSalle(salle);

        problem.getSeances().addAll(java.util.List.of(s1, s2));
        problem.getCreneaux().addAll(java.util.List.of(c1, c2));
        problem.getSalles().add(salle);
        problem.getProfesseurs().add(prof);

        constraintVerifier.verifyThat(PlanningConstraints::teacherMaxHoursPerDay)
                .givenSolution(problem)
                .penalizesBy(1);
    }

    @Test
    void teacherMaxHoursPerWeek() {
        Planning problem = new Planning();
        Creneau c1 = new Creneau();
        c1.setId(1L);
        c1.setDebut(LocalDateTime.of(2024, 2, 12, 8, 0));
        c1.setFin(LocalDateTime.of(2024, 2, 12, 18, 0));

        Creneau c2 = new Creneau();
        c2.setId(2L);
        c2.setDebut(LocalDateTime.of(2024, 2, 13, 8, 0)); // Same week (ISO week 7)
        c2.setFin(LocalDateTime.of(2024, 2, 13, 18, 0));

        Salle salle = new Salle();
        salle.setId(1L);

        Professeur prof = new Professeur();
        prof.setId(1L);
        prof.setMaxHeuresParSemaine(BigDecimal.valueOf(15));

        Seance s1 = new Seance();
        s1.setId(1L);
        s1.setProfesseur(prof);
        s1.setCreneau(c1);
        s1.setSalle(salle);

        Seance s2 = new Seance();
        s2.setId(2L);
        s2.setProfesseur(prof);
        s2.setCreneau(c2);
        s2.setSalle(salle);

        problem.getSeances().addAll(java.util.List.of(s1, s2));
        problem.getCreneaux().addAll(java.util.List.of(c1, c2));
        problem.getSalles().add(salle);
        problem.getProfesseurs().add(prof);

        constraintVerifier.verifyThat(PlanningConstraints::teacherMaxHoursPerWeek)
                .givenSolution(problem)
                .penalizesBy(1);
    }

    @Test
    void teacherMaxHoursPerSession() {
        Planning problem = new Planning();
        Creneau c1 = new Creneau();
        c1.setId(1L);
        c1.setDebut(LocalDateTime.of(2024, 2, 12, 8, 0));
        c1.setFin(LocalDateTime.of(2024, 2, 12, 12, 0));

        Salle salle = new Salle();
        salle.setId(1L);

        Professeur prof = new Professeur();
        prof.setId(1L);
        prof.setMaxHeuresParSeance(BigDecimal.valueOf(3));

        Seance s1 = new Seance();
        s1.setId(1L);
        s1.setProfesseur(prof);
        s1.setCreneau(c1);
        s1.setSalle(salle);

        problem.getSeances().add(s1);
        problem.getCreneaux().add(c1);
        problem.getSalles().add(salle);
        problem.getProfesseurs().add(prof);

        constraintVerifier.verifyThat(PlanningConstraints::teacherMaxHoursPerSession)
                .givenSolution(problem)
                .penalizesBy(1);
    }

    @Test
    void teacherClassMaxHoursConsecutive() {
        Planning problem = new Planning();
        Creneau day1 = new Creneau();
        day1.setId(1L);
        day1.setDebut(LocalDateTime.of(2024, 2, 12, 8, 0));
        day1.setFin(LocalDateTime.of(2024, 2, 12, 11, 0));

        Creneau day2 = new Creneau();
        day2.setId(2L);
        day2.setDebut(LocalDateTime.of(2024, 2, 13, 8, 0));
        day2.setFin(LocalDateTime.of(2024, 2, 13, 11, 0));

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
        s1.setCreneau(day1);
        s1.setSalle(salle);

        Seance s2 = new Seance();
        s2.setId(2L);
        s2.setProfesseur(prof);
        s2.setClasse(classe);
        s2.setCreneau(day2);
        s2.setSalle(salle);

        problem.getSeances().addAll(java.util.List.of(s1, s2));
        problem.getCreneaux().addAll(java.util.List.of(day1, day2));
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
        Creneau creneau = new Creneau();
        creneau.setId(1L);
        creneau.setDebut(LocalDateTime.of(2024, 2, 12, 8, 0));
        creneau.setFin(LocalDateTime.of(2024, 2, 12, 9, 0));

        Salle salle = new Salle();
        salle.setId(1L);

        Classe classe = new Classe();
        classe.setId(1L);

        Professeur prof = new Professeur();
        prof.setId(1L);

        Seance s1 = new Seance();
        s1.setId(1L);
        s1.setClasse(classe);
        s1.setCreneau(creneau);
        s1.setSalle(salle);
        s1.setProfesseur(prof);

        problem.getSeances().add(s1);
        problem.getCreneaux().add(creneau);
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
    void studentGroupWeekTypeMismatch() {
        Planning problem = new Planning();
        
        Classe classe = new Classe();
        classe.setId(1L);

        ClassePresence presence = new ClassePresence();
        presence.setId(1L);
        presence.setClasse(classe);
        presence.setDateDebut(LocalDate.of(2024, 2, 12));
        presence.setDateFin(LocalDate.of(2024, 2, 25));

        Creneau c1 = new Creneau();
        c1.setId(1L);
        c1.setDebut(LocalDateTime.of(2024, 2, 14, 10, 0));
        c1.setFin(LocalDateTime.of(2024, 2, 14, 12, 0));
        c1.setSemaineType(SemaineType.SEMAINE_1);

        Creneau c2 = new Creneau();
        c2.setId(2L);
        c2.setDebut(LocalDateTime.of(2024, 2, 21, 10, 0));
        c2.setFin(LocalDateTime.of(2024, 2, 21, 12, 0));
        c2.setSemaineType(SemaineType.SEMAINE_1);

        Salle salle = new Salle();
        salle.setId(1L);

        Professeur prof = new Professeur();
        prof.setId(1L);

        Seance s1 = new Seance();
        s1.setId(1L);
        s1.setClasse(classe);
        s1.setCreneau(c1);
        s1.setSalle(salle);
        s1.setProfesseur(prof);

        Seance s2 = new Seance();
        s2.setId(2L);
        s2.setClasse(classe);
        s2.setCreneau(c2);
        s2.setSalle(salle);
        s2.setProfesseur(prof);

        problem.getSeances().addAll(java.util.List.of(s1, s2));
        problem.getCreneaux().addAll(java.util.List.of(c1, c2));
        problem.getSalles().add(salle);
        problem.getProfesseurs().add(prof);
        problem.getClasses().add(classe);
        problem.getClassePresences().add(presence);

        constraintVerifier.verifyThat(PlanningConstraints::studentGroupWeekTypeMismatch)
                .givenSolution(problem)
                .penalizesBy(1);
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
        Creneau c_monday_ok = new Creneau();
        c_monday_ok.setId(1L);
        c_monday_ok.setDebut(LocalDateTime.of(2024, 2, 12, 9, 0));
        c_monday_ok.setFin(LocalDateTime.of(2024, 2, 12, 10, 0));

        // Valid Friday slot: last Friday (2024-02-23) 10h-11h
        Creneau c_friday_ok = new Creneau();
        c_friday_ok.setId(2L);
        c_friday_ok.setDebut(LocalDateTime.of(2024, 2, 23, 10, 0));
        c_friday_ok.setFin(LocalDateTime.of(2024, 2, 23, 11, 0));

        // Invalid Monday slot (wrong time): first Monday 10h-11h
        Creneau c_monday_wrong_time = new Creneau();
        c_monday_wrong_time.setId(3L);
        c_monday_wrong_time.setDebut(LocalDateTime.of(2024, 2, 12, 10, 0));
        c_monday_wrong_time.setFin(LocalDateTime.of(2024, 2, 12, 11, 0));

        // Invalid Friday slot (wrong time): last Friday 9h-10h
        Creneau c_friday_wrong_time = new Creneau();
        c_friday_wrong_time.setId(4L);
        c_friday_wrong_time.setDebut(LocalDateTime.of(2024, 2, 23, 9, 0));
        c_friday_wrong_time.setFin(LocalDateTime.of(2024, 2, 23, 10, 0));

        // Invalid slot (wrong date): middle Friday (2024-02-16) 10h-11h
        Creneau c_wrong_date = new Creneau();
        c_wrong_date.setId(5L);
        c_wrong_date.setDebut(LocalDateTime.of(2024, 2, 16, 10, 0));
        c_wrong_date.setFin(LocalDateTime.of(2024, 2, 16, 11, 0));

        Salle salle = new Salle();
        salle.setId(1L);

        Professeur prof = new Professeur();
        prof.setId(1L);

        Seance s_monday_ok = new Seance();
        s_monday_ok.setId(1L);
        s_monday_ok.setClasse(classe);
        s_monday_ok.setType(Seance.TypeSeance.VIE_DE_CLASSE);
        s_monday_ok.setCreneau(c_monday_ok);
        s_monday_ok.setSalle(salle);
        s_monday_ok.setProfesseur(prof);

        Seance s_friday_ok = new Seance();
        s_friday_ok.setId(2L);
        s_friday_ok.setClasse(classe);
        s_friday_ok.setType(Seance.TypeSeance.VIE_DE_CLASSE);
        s_friday_ok.setCreneau(c_friday_ok);
        s_friday_ok.setSalle(salle);
        s_friday_ok.setProfesseur(prof);

        Seance s_monday_wrong_time = new Seance();
        s_monday_wrong_time.setId(3L);
        s_monday_wrong_time.setClasse(classe);
        s_monday_wrong_time.setType(Seance.TypeSeance.VIE_DE_CLASSE);
        s_monday_wrong_time.setCreneau(c_monday_wrong_time);
        s_monday_wrong_time.setSalle(salle);
        s_monday_wrong_time.setProfesseur(prof);

        Seance s_friday_wrong_time = new Seance();
        s_friday_wrong_time.setId(4L);
        s_friday_wrong_time.setClasse(classe);
        s_friday_wrong_time.setType(Seance.TypeSeance.VIE_DE_CLASSE);
        s_friday_wrong_time.setCreneau(c_friday_wrong_time);
        s_friday_wrong_time.setSalle(salle);
        s_friday_wrong_time.setProfesseur(prof);

        Seance s_wrong_date = new Seance();
        s_wrong_date.setId(5L);
        s_wrong_date.setClasse(classe);
        s_wrong_date.setType(Seance.TypeSeance.VIE_DE_CLASSE);
        s_wrong_date.setCreneau(c_wrong_date);
        s_wrong_date.setSalle(salle);
        s_wrong_date.setProfesseur(prof);

        problem.getSeances().addAll(java.util.List.of(s_monday_ok, s_friday_ok, s_monday_wrong_time, s_friday_wrong_time, s_wrong_date));
        problem.getCreneaux().addAll(java.util.List.of(c_monday_ok, c_friday_ok, c_monday_wrong_time, c_friday_wrong_time, c_wrong_date));
        problem.getSalles().add(salle);
        problem.getProfesseurs().add(prof);
        problem.getClasses().add(classe);
        problem.getClassePresences().add(presence);

        constraintVerifier.verifyThat(PlanningConstraints::vieDeClasseTimingConstraint)
                .givenSolution(problem)
                .penalizesBy(3);
    }

    @Test
    void seanceTypeDurationMatch() {
        Planning problem = new Planning();

        Creneau c1 = new Creneau();
        c1.setId(1L);
        c1.setDebut(LocalDateTime.of(2024, 2, 12, 8, 0));
        c1.setFin(LocalDateTime.of(2024, 2, 12, 9, 30)); // 1h30 (90 mins)

        Creneau c2 = new Creneau();
        c2.setId(2L);
        c2.setDebut(LocalDateTime.of(2024, 2, 12, 10, 0));
        c2.setFin(LocalDateTime.of(2024, 2, 12, 11, 0)); // 1h (60 mins)

        Salle salle = new Salle();
        salle.setId(1L);

        Professeur prof = new Professeur();
        prof.setId(1L);

        // Seance 1: TP on a 1.5h slot (matching) -> no penalty
        Seance s1 = new Seance();
        s1.setId(1L);
        s1.setType(Seance.TypeSeance.TP);
        s1.setCreneau(c1);
        s1.setSalle(salle);
        s1.setProfesseur(prof);

        // Seance 2: COURS on a 1.5h slot (mismatching) -> 1 penalty
        Seance s2 = new Seance();
        s2.setId(2L);
        s2.setType(Seance.TypeSeance.COURS);
        s2.setCreneau(c1);
        s2.setSalle(salle);
        s2.setProfesseur(prof);

        // Seance 3: TP on a 1h slot (mismatching) -> 1 penalty
        Seance s3 = new Seance();
        s3.setId(3L);
        s3.setType(Seance.TypeSeance.TP);
        s3.setCreneau(c2);
        s3.setSalle(salle);
        s3.setProfesseur(prof);

        // Seance 4: COURS on a 1h slot (matching) -> no penalty
        Seance s4 = new Seance();
        s4.setId(4L);
        s4.setType(Seance.TypeSeance.COURS);
        s4.setCreneau(c2);
        s4.setSalle(salle);
        s4.setProfesseur(prof);

        problem.getSeances().addAll(java.util.List.of(s1, s2, s3, s4));
        problem.getCreneaux().addAll(java.util.List.of(c1, c2));
        problem.getSalles().add(salle);
        problem.getProfesseurs().add(prof);

        constraintVerifier.verifyThat(PlanningConstraints::seanceTypeDurationMatch)
                .givenSolution(problem)
                .penalizesBy(2);
    }
}
