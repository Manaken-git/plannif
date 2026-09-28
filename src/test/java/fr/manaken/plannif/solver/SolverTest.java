package fr.manaken.plannif.solver;

import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.SolverConfig;
import fr.manaken.plannif.business.Planning;
import fr.manaken.plannif.business.PlanningConstraints;
import fr.manaken.plannif.model.Salle;
import fr.manaken.plannif.model.Seance;
import fr.manaken.plannif.model.Professeur;
import fr.manaken.plannif.model.Classe;
import fr.manaken.plannif.model.Matiere;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SolverTest {

    @Test
    public void testSolver() {
        SolverConfig solverConfig = new SolverConfig()
                .withSolutionClass(Planning.class)
                .withEntityClasses(Seance.class)
                .withConstraintProviderClass(PlanningConstraints.class)
                .withTerminationSpentLimit(Duration.ofSeconds(4));

        SolverFactory<Planning> solverFactory = SolverFactory.create(solverConfig);
        Solver<Planning> solver = solverFactory.buildSolver();

        Planning problem = generateProblem();
        Planning solution = solver.solve(problem);

        assertNotNull(solution);
        assertNotNull(solution.getScore());
        assertTrue(solution.getScore().isFeasible(), "Solution should be feasible");

        for (Seance seance : solution.getSeances()) {
            assertNotNull(seance.getSalle(), "Seance " + seance.getId() + " should have a room assigned");
            System.out.println("Seance " + seance.getId() + " -> " + seance.getSalle().getCode());
        }
    }

    private Planning generateProblem() {
        Planning planning = new Planning();

        // Dates debut possibles
        LocalDateTime debut = LocalDateTime.of(2024, 2, 12, 8, 0);
        planning.getDatesDebutPossibles().add(debut);

        // Salles
        Salle s1 = new Salle();
        s1.setId(1L);
        s1.setCode("A101");

        Salle s2 = new Salle();
        s2.setId(2L);
        s2.setCode("A102");

        List<Salle> salles = new ArrayList<>();
        salles.add(s1);
        salles.add(s2);
        planning.setSalles(salles);

        // Profs
        Professeur p1 = new Professeur();
        p1.setId(1L);
        p1.setNom("Prof1");

        Professeur p2 = new Professeur();
        p2.setId(2L);
        p2.setNom("Prof2");

        // Classes
        Classe cl1 = new Classe();
        cl1.setId(1L);
        cl1.setNom("Class1");

        Classe cl2 = new Classe();
        cl2.setId(2L);
        cl2.setNom("Class2");

        planning.setProfesseurs(List.of(p1, p2));
        planning.setClasses(List.of(cl1, cl2));

        // Matieres
        Matiere m1 = new Matiere();
        m1.setId(1L);
        m1.setNom("Maths");

        p1.getMatieres().add(m1);
        p2.getMatieres().add(m1);

        // Presences (mandatory for feasibility)
        fr.manaken.plannif.model.ClassePresence cp1 = new fr.manaken.plannif.model.ClassePresence();
        cp1.setId(1L);
        cp1.setClasse(cl1);
        cp1.setDateDebut(LocalDate.of(2024, 2, 12));
        cp1.setDateFin(LocalDate.of(2024, 2, 12));

        fr.manaken.plannif.model.ClassePresence cp2 = new fr.manaken.plannif.model.ClassePresence();
        cp2.setId(2L);
        cp2.setClasse(cl2);
        cp2.setDateDebut(LocalDate.of(2024, 2, 12));
        cp2.setDateFin(LocalDate.of(2024, 2, 12));

        planning.setClassePresences(List.of(cp1, cp2));

        // Seances
        Seance seance1 = new Seance();
        seance1.setId(1L);
        seance1.setProfesseur(p1);
        seance1.setClasse(cl1);
        seance1.setMatiere(m1);
        seance1.setDebut(debut); // Fixed time

        Seance seance2 = new Seance();
        seance2.setId(2L);
        seance2.setProfesseur(p2);
        seance2.setClasse(cl2);
        seance2.setMatiere(m1);
        seance2.setDebut(debut); // Fixed time, same as seance1

        List<Seance> seances = new ArrayList<>();
        seances.add(seance1);
        seances.add(seance2);
        planning.setSeances(seances);

        return planning;
    }
}
