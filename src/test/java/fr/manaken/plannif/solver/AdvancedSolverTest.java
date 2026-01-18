package fr.manaken.plannif.solver;

import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.SolverConfig;
import fr.manaken.plannif.TestDataFactory;
import fr.manaken.plannif.business.Planning;
import fr.manaken.plannif.business.PlanningConstraints;
import fr.manaken.plannif.model.ClassePresence;
import fr.manaken.plannif.model.Seance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

public class AdvancedSolverTest {

    private SolverFactory<Planning> solverFactory;
    private SolutionManager<Planning, HardSoftScore> solutionManager;

    @BeforeEach
    void setUp() {
        SolverConfig solverConfig = new SolverConfig()
                .withSolutionClass(Planning.class)
                .withEntityClasses(Seance.class)
                .withConstraintProviderClass(PlanningConstraints.class)
                .withTerminationSpentLimit(Duration.ofSeconds(1));

        solverFactory = SolverFactory.create(solverConfig);
        solutionManager = SolutionManager.create(solverFactory);
    }

    @Test
    void shouldDetectRoomConflict() {
        // Given
        Planning problem = TestDataFactory.generateProblem();
        // Force two seances in the same room at the same time
        Seance s1 = problem.getSeances().get(0);
        Seance s2 = problem.getSeances().get(1);

        s1.setSalle(problem.getSalles().get(0));
        s2.setSalle(problem.getSalles().get(0));

        // When
        HardSoftScore score = solutionManager.update(problem);

        // Then
        assertThat(score.hardScore()).isNegative();
    }

    @Test
    void shouldDetectTeacherConflict() {
        // Given
        Planning problem = TestDataFactory.generateProblem();
        Seance s1 = problem.getSeances().get(0);
        Seance s2 = problem.getSeances().get(1);

        // Force same teacher for different classes at the same time
        s2.setProfesseur(s1.getProfesseur());

        // Initialize rooms to avoid "uninitialized" state
        s1.setSalle(problem.getSalles().get(0));
        s2.setSalle(problem.getSalles().get(1));

        // When
        HardSoftScore score = solutionManager.update(problem);
        System.out.println("Score explanation: " + solutionManager.explain(problem).getSummary());

        // Then
        assertThat(score.hardScore()).isNegative();
    }

    @Test
    void shouldDetectStudentGroupMissingPresence() {
        // Given
        Planning problem = TestDataFactory.generateProblem();
        for (int i = 0; i < problem.getSeances().size(); i++) {
            problem.getSeances().get(i).setSalle(problem.getSalles().get(i % problem.getSalles().size()));
        }

        // When
        HardSoftScore score = solutionManager.update(problem);

        // Then
        // Since there are no ClassePresence, every Seance is a violation
        assertThat(score.hardScore()).isEqualTo(-2);
    }

    @Test
    void shouldDetectStudentGroupPresenceValid() {
        // Given
        Planning problem = TestDataFactory.generateProblem();
        for (int i = 0; i < problem.getSeances().size(); i++) {
            problem.getSeances().get(i).setSalle(problem.getSalles().get(i % problem.getSalles().size()));
        }

        Seance s1 = problem.getSeances().get(0);

        // Add presence covering the seance date (2024-01-01)
        ClassePresence presence = new ClassePresence();
        presence.setClasse(s1.getClasse());
        presence.setDateDebut(LocalDate.of(2024, 1, 1));
        presence.setDateFin(LocalDate.of(2024, 1, 1));
        problem.setClassePresences(List.of(presence));

        // When
        HardSoftScore score = solutionManager.update(problem);

        // Then
        // We still have the other seance (s2) without presence, so score should be
        // -1hard
        assertThat(score.hardScore()).isEqualTo(-1);
    }
}
