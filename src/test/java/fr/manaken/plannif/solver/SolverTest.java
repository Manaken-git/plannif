package fr.manaken.plannif.solver;

import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.SolverConfig;
import fr.manaken.plannif.business.Planning;
import fr.manaken.plannif.business.PlanningConstraints;
import fr.manaken.plannif.model.Creneau;
import fr.manaken.plannif.model.Salle;
import fr.manaken.plannif.model.Seance;
import fr.manaken.plannif.model.Professeur;
import fr.manaken.plannif.model.Classe;
import fr.manaken.plannif.model.Matiere;
import org.junit.jupiter.api.Test;

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

        // Creneau
        Creneau c1 = new Creneau();
        c1.setId(1L);
        c1.setDebut(LocalDateTime.of(2024, 1, 1, 8, 0));
        c1.setFin(LocalDateTime.of(2024, 1, 1, 9, 0));

        List<Creneau> creneaux = new ArrayList<>();
        creneaux.add(c1);
        planning.setCreneaux(creneaux);

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

        // Matieres
        Matiere m1 = new Matiere();
        m1.setId(1L);
        m1.setNom("Maths");

        // Seances
        Seance seance1 = new Seance();
        seance1.setId(1L);
        seance1.setProfesseur(p1);
        seance1.setClasse(cl1);
        seance1.setMatiere(m1);
        seance1.setCreneau(c1); // Fixed time

        Seance seance2 = new Seance();
        seance2.setId(2L);
        seance2.setProfesseur(p2);
        seance2.setClasse(cl2);
        seance2.setMatiere(m1);
        seance2.setCreneau(c1); // Fixed time, same as seance1

        List<Seance> seances = new ArrayList<>();
        seances.add(seance1);
        seances.add(seance2);
        planning.setSeances(seances);

        return planning;
    }
}
