package fr.manaken.plannif.controller;

import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.SolverConfig;
import fr.manaken.plannif.business.Planning;
import fr.manaken.plannif.business.PlanningConstraints;
import fr.manaken.plannif.exporter.PlanningExporter;
import fr.manaken.plannif.model.Seance;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class PlanningControllerTest {

    static final String FICHIER_TEST = "test_data_scenario.json";
    static final String FICHIER_BIG_TEST = "test_big_data_scenario.json";

    @Test
    void testSolver() {
        // 1. Configure Solver
        SolverConfig solverConfig = new SolverConfig()
                .withSolutionClass(Planning.class)
                .withEntityClasses(Seance.class)
                .withConstraintProviderClass(PlanningConstraints.class)
                .withTerminationSpentLimit(Duration.ofSeconds(4));

        SolverFactory<Planning> solverFactory = SolverFactory.create(solverConfig);
        Solver<Planning> solver = solverFactory.buildSolver();

        // 2. Generate Data
        Planning problem = generateProblem(FICHIER_BIG_TEST);

        // 3. Solve
        Planning solution = solver.solve(problem);

        // 4. Verify Result
        assertThat(solution).isNotNull();

        // 5. Export result to HTML Gantt view
        PlanningExporter.exportToHtml(solution, "planning_result.html");
        System.out.println("Gantt result written to: " + new java.io.File("planning_result.html").getAbsolutePath());
    }

    private Planning generateProblem(String fichier) {
        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper()
                .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
                .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try (java.io.InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream(fichier)) {
            Planning planning = objectMapper.readValue(inputStream, Planning.class);
            try (java.io.InputStream is2 = getClass().getClassLoader().getResourceAsStream(fichier)) {
                com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.readTree(is2);
                
                com.fasterxml.jackson.databind.JsonNode profsNode = rootNode.get("professeurs");
                if (profsNode != null && profsNode.isArray()) {
                    for (com.fasterxml.jackson.databind.JsonNode pn : profsNode) {
                        if (pn.has("matiere_ids")) {
                            Long profId = pn.get("id").asLong();
                            planning.getProfesseurs().stream()
                                .filter(p -> p.getId().equals(profId))
                                .findFirst()
                                .ifPresent(prof -> {
                                    for (com.fasterxml.jackson.databind.JsonNode midNode : pn.get("matiere_ids")) {
                                        Long mid = midNode.asLong();
                                        planning.getMatieres().stream()
                                            .filter(m -> m.getId().equals(mid))
                                            .findFirst()
                                            .ifPresent(prof.getMatieres()::add);
                                    }
                                });
                        }
                    }
                }

                com.fasterxml.jackson.databind.JsonNode seancesNode = rootNode.get("seances_exemples");
                if (seancesNode != null && seancesNode.isArray()) {
                    for (com.fasterxml.jackson.databind.JsonNode sn : seancesNode) {
                        Seance s = new Seance();
                        s.setId(sn.get("id").asLong());
                        Long profId = sn.get("professeur_id").asLong();
                        planning.getProfesseurs().stream().filter(p -> p.getId().equals(profId)).findFirst()
                                .ifPresent(s::setProfesseur);
                        Long classeId = sn.get("classe_id").asLong();
                        planning.getClasses().stream().filter(c -> c.getId().equals(classeId)).findFirst()
                                .ifPresent(s::setClasse);
                        if (sn.has("matiere_id")) {
                            Long matiereId = sn.get("matiere_id").asLong();
                            planning.getMatieres().stream().filter(m -> m.getId().equals(matiereId)).findFirst()
                                    .ifPresent(s::setMatiere);
                        }
                        planning.getSeances().add(s);
                    }
                }
            }
            return planning;
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to read " + fichier, e);
        }
    }

}
