package fr.manaken.plannif.solver;

import ai.timefold.solver.test.api.score.stream.ConstraintVerifier;
import fr.manaken.plannif.business.Planning;
import fr.manaken.plannif.business.PlanningConstraints;
import fr.manaken.plannif.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

class ConstraintVerifierTest {

    ConstraintVerifier<PlanningConstraints, Planning> constraintVerifier = ConstraintVerifier.build(
            new PlanningConstraints(), Planning.class, Seance.class);

    @Test
    void roomConflict() {
        Planning problem = new Planning();
        Creneau creneau = new Creneau();
        creneau.setId(1L);
        creneau.setDebut(LocalTime.of(8, 0));
        creneau.setFin(LocalTime.of(9, 0));

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
        creneau.setDebut(LocalTime.of(8, 0));
        creneau.setFin(LocalTime.of(9, 0));

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
        creneau.setDebut(LocalTime.of(8, 0));
        creneau.setFin(LocalTime.of(9, 0));

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
        mondayCreneau.setDebut(LocalTime.of(8, 0));
        mondayCreneau.setFin(LocalTime.of(9, 0));

        Salle salle = new Salle();
        salle.setId(1L);

        Professeur prof = new Professeur();
        prof.setId(1L);

        ProfesseurDayOff dayOff = new ProfesseurDayOff();
        dayOff.setId(1L);
        dayOff.setProfesseur(prof);
        dayOff.setDayOfWeek(0);

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
        c1.setDebut(LocalTime.of(8, 0));
        c1.setFin(LocalTime.of(12, 0));

        Creneau c2 = new Creneau();
        c2.setId(2L);
        c2.setDebut(LocalTime.of(14, 0));
        c2.setFin(LocalTime.of(18, 0));

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
        c1.setDebut(LocalTime.of(8, 0));
        c1.setFin(LocalTime.of(18, 0));

        Creneau c2 = new Creneau();
        c2.setId(2L);
        c2.setDebut(LocalTime.of(8, 0));
        c2.setFin(LocalTime.of(18, 0));

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
        c1.setDebut(LocalTime.of(8, 0));
        c1.setFin(LocalTime.of(12, 0));

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
        day1.setDebut(LocalTime.of(8, 0));
        day1.setFin(LocalTime.of(11, 0));

        Creneau day2 = new Creneau();
        day2.setId(2L);
        day2.setDebut(LocalTime.of(8, 0));
        day2.setFin(LocalTime.of(11, 0));

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
        creneau.setDebut(LocalTime.of(8, 0));
        creneau.setFin(LocalTime.of(9, 0));

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

        // Presence exists
        ClassePresence presence = new ClassePresence();
        presence.setId(1L);
        presence.setClasse(classe);
        presence.setDateDebut(LocalDate.of(2024, 1, 1));
        presence.setDateFin(LocalDate.of(2024, 1, 1));

        problem.getClassePresences().add(presence);

        constraintVerifier.verifyThat(PlanningConstraints::studentGroupPresence)
                .givenSolution(problem)
                .penalizesBy(0);
    }
}
