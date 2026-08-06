package fr.manaken.plannif.controller;

import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.SolverConfig;
import fr.manaken.plannif.business.Planning;
import fr.manaken.plannif.business.PlanningConstraints;
import fr.manaken.plannif.model.*;
import fr.manaken.plannif.service.PlanningService;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/planning")
@CrossOrigin(origins = "http://localhost:5173")
public class PlanningController {

    private final PlanningService planningService;

    public PlanningController(PlanningService planningService) {
        this.planningService = planningService;
    }

    private SolverFactory<Planning> getSolverFactory() {
        SolverConfig solverConfig = new SolverConfig()
                .withSolutionClass(Planning.class)
                .withEntityClasses(Seance.class)
                .withConstraintProviderClass(PlanningConstraints.class)
                .withTerminationSpentLimit(Duration.ofSeconds(30));

        return SolverFactory.create(solverConfig);
    }

    /**
     * Endpoint principal : reçoit un Planning (JSON) en entrée,
     * lance le solver Timefold, et retourne le Planning résolu.
     */
    @PostMapping("/solve")
    public Planning solve(@RequestBody Planning problem) {
        SolverFactory<Planning> solverFactory = getSolverFactory();
        Solver<Planning> solver = solverFactory.buildSolver();
        planningService.generateCreneauxIfNeeded(problem);
        planningService.generateSeancesIfNeeded(problem);
        Planning solution = solver.solve(problem);

        // Explication du score dans la console
        var solutionManager = SolutionManager.create(solverFactory);
        var explanation = solutionManager.explain(solution);
        System.out.println("=========================================================================");
        System.out.println("TIMEFOLD SOLVER SCORE EXPLANATION :");
        System.out.println(explanation.getSummary());
        System.out.println("=========================================================================");

        return solution;
    }

    @GetMapping("/solve")
    public Planning solve() {
        SolverConfig solverConfig = SolverConfig.createFromXmlResource("solverConfig.xml");
        SolverFactory<Planning> solverFactory = SolverFactory.create(solverConfig);
        Solver<Planning> solver = solverFactory.buildSolver();

        Planning problem = planningService.buildPlanning();
        planningService.generateCreneauxIfNeeded(problem);
        planningService.generateSeancesIfNeeded(problem);
        Planning solution = solver.solve(problem);

        // Explication du score dans la console
        var solutionManager = SolutionManager.create(solverFactory);
        var explanation = solutionManager.explain(solution);
        System.out.println("=========================================================================");
        System.out.println("TIMEFOLD SOLVER SCORE EXPLANATION :");
        System.out.println(explanation.getSummary());
        System.out.println("=========================================================================");

        return solution;
    }

    @GetMapping("/solve-html")
    public String solveHtml() {
        SolverConfig solverConfig = SolverConfig.createFromXmlResource("solverConfig.xml");
        SolverFactory<Planning> solverFactory = SolverFactory.create(solverConfig);
        Solver<Planning> solver = solverFactory.buildSolver();

        Planning problem = generateProblem();
        Planning solution = solver.solve(problem);

        // Explication du score dans la console
        var solutionManager = SolutionManager.create(solverFactory);
        var explanation = solutionManager.explain(solution);
        System.out.println("=========================================================================");
        System.out.println("TIMEFOLD SOLVER SCORE EXPLANATION (HTML version) :");
        System.out.println(explanation.getSummary());
        System.out.println("=========================================================================");

        StringBuilder sb = new StringBuilder();
        sb.append("<h1>Planning Result</h1>");
        sb.append("<p>Score: ").append(solution.getScore()).append("</p>");
        sb.append("<table border='1'><tr><th>Seance</th><th>Prof</th><th>Class</th><th>Room</th></tr>");

        for (Seance seance : solution.getSeances()) {
            sb.append("<tr>");
            sb.append("<td>").append(seance.getId()).append("</td>");
            sb.append("<td>").append(seance.getProfesseur().getNom()).append("</td>");
            sb.append("<td>").append(seance.getClasse().getNom()).append("</td>");
            sb.append("<td>").append(seance.getSalle() != null ? seance.getSalle().getCode() : "Unassigned")
                    .append("</td>");
            sb.append("</tr>");
        }
        sb.append("</table>");
        return sb.toString();
    }

    private Planning generateProblem() {
        Planning planning = new Planning();

        // Creneau
        Creneau c1 = new Creneau();
        c1.setId(1L);
        c1.setDebut(LocalDateTime.of(2024, 2, 12, 8, 0));
        c1.setFin(LocalDateTime.of(2024, 2, 12, 9, 0));
        c1.setSemaineType(SemaineType.SEMAINE_1);

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

        planning.getProfesseurs().add(p1);
        planning.getProfesseurs().add(p2);

        // Classes
        Classe cl1 = new Classe();
        cl1.setId(1L);
        cl1.setNom("Class1");

        Classe cl2 = new Classe();
        cl2.setId(2L);
        cl2.setNom("Class2");

        planning.getClasses().add(cl1);
        planning.getClasses().add(cl2);

        // Matieres
        Matiere m1 = new Matiere();
        m1.setId(1L);
        m1.setNom("Maths");
        planning.getMatieres().add(m1);

        // Seances
        Seance seance1 = new Seance();
        seance1.setId(1L);
        seance1.setProfesseur(p1);
        seance1.setClasse(cl1);
        seance1.setMatiere(m1);
        seance1.setCreneau(c1);

        Seance seance2 = new Seance();
        seance2.setId(2L);
        seance2.setProfesseur(p2);
        seance2.setClasse(cl2);
        seance2.setMatiere(m1);
        seance2.setCreneau(c1);

        List<Seance> seances = new ArrayList<>();
        seances.add(seance1);
        seances.add(seance2);
        planning.setSeances(seances);

        planning.setProfesseurDayOffs(new ArrayList<>());
        planning.setClassePresences(new ArrayList<>());

        return planning;
    }
}
