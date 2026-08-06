package fr.manaken.plannif.controller;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.SolverConfig;
import fr.manaken.plannif.business.Planning;
import fr.manaken.plannif.business.PlanningConstraints;
import fr.manaken.plannif.exporter.PlanningExporter;
import fr.manaken.plannif.model.MatiereClasseConfig;
import fr.manaken.plannif.model.Seance;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class PlanningControllerTest {

    static final String FICHIER_TEST = "test_data_scenario.json";
    static final String FICHIER_BIG_TEST = "test_big_data_scenario.json";
    static final String FICHIER_SANS_SEANCE = "test_big_data_scenario_sans_seance.json";

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

        // 5. Verify and Export
        assertThat(solution).isNotNull();
        System.out.println("Final Score: " + solution.getScore());
        
        // Explain score
        ai.timefold.solver.core.api.solver.SolutionManager<Planning, HardSoftScore> solutionManager = ai.timefold.solver.core.api.solver.SolutionManager.create(solverFactory);
        System.out.println(solutionManager.explain(solution));

        PlanningExporter.exportToHtml(solution, "planning_result.html");
        System.out.println("Gantt result written to: " + new java.io.File("planning_result.html").getAbsolutePath());
    }

    @Test
    void testSolverWithGeneration() {
        // 1. Configure Solver
        SolverConfig solverConfig = new SolverConfig()
                .withSolutionClass(Planning.class)
                .withEntityClasses(Seance.class)
                .withConstraintProviderClass(PlanningConstraints.class)
                .withTerminationSpentLimit(Duration.ofSeconds(20));

        SolverFactory<Planning> solverFactory = SolverFactory.create(solverConfig);
        Solver<Planning> solver = solverFactory.buildSolver();

        // 2. Generate Global Fact Data from JSON (without sessions)
        Planning problem = generateProblem(FICHIER_SANS_SEANCE);

        // 3. Programmatically generate sessions (4 per class)
        // Only pick subjects that have at least one qualified teacher
        java.util.List<fr.manaken.plannif.model.Matiere> validMatieres = problem.getMatieres().stream()
                .filter(m -> problem.getProfesseurs().stream().anyMatch(p -> p.getMatieres().contains(m)))
                .collect(java.util.stream.Collectors.toList());

        long seanceId = 1000;
        java.util.Random random = new java.util.Random(42);
        for (fr.manaken.plannif.model.Classe classe : problem.getClasses()) {
            for (int i = 0; i < 4; i++) {
                Seance s = new Seance();
                s.setId(seanceId++);
                s.setClasse(classe);
                // Pick a random valid subject
                fr.manaken.plannif.model.Matiere matiere = validMatieres.get(random.nextInt(validMatieres.size()));
                s.setMatiere(matiere);
                problem.getSeances().add(s);
            }
        }

        // 4. Solve
        Planning solution = solver.solve(problem);

        // 5. Verify and Export
        assertThat(solution).isNotNull();
        System.out.println("Final Score (Generated): " + solution.getScore());
        
        // Explain score
        ai.timefold.solver.core.api.solver.SolutionManager<Planning, HardSoftScore> solutionManager = ai.timefold.solver.core.api.solver.SolutionManager.create(solverFactory);
        System.out.println(solutionManager.explain(solution));

        PlanningExporter.exportToHtml(solution, "planning_result_generated.html");
        System.out.println("Generated Gantt result written to: " + new java.io.File("planning_result_generated.html").getAbsolutePath());
    }

    @Test
    void testGenerateCreneaux() {
        fr.manaken.plannif.service.PlanningService planningService = new fr.manaken.plannif.service.PlanningService(
                null, null, null, null, null, null, null, null, null
        );

        Planning planning = new Planning();
        planning.setCreneaux(new java.util.ArrayList<>());

        fr.manaken.plannif.model.ClassePresence presence = new fr.manaken.plannif.model.ClassePresence();
        presence.setId(1L);
        presence.setDateDebut(LocalDate.of(2024, 2, 12)); // Monday
        presence.setDateFin(LocalDate.of(2024, 2, 16));   // Friday
        planning.setClassePresences(java.util.List.of(presence));

        planningService.generateCreneauxIfNeeded(planning);

        assertThat(planning.getCreneaux()).isNotEmpty();
        // 5 days * (8 default 1h slots + 6 TP 1h30 slots) = 70 slots
        assertThat(planning.getCreneaux()).hasSize(70);

        fr.manaken.plannif.model.Creneau first = planning.getCreneaux().get(0);
        assertThat(first.getDebut()).isEqualTo(LocalDateTime.of(2024, 2, 12, 8, 0));
        assertThat(first.getFin()).isEqualTo(LocalDateTime.of(2024, 2, 12, 9, 0));
        assertThat(first.getSemaineType()).isEqualTo(fr.manaken.plannif.model.SemaineType.SEMAINE_1);

        fr.manaken.plannif.model.Creneau last = planning.getCreneaux().get(69);
        assertThat(last.getDebut()).isEqualTo(LocalDateTime.of(2024, 2, 16, 16, 0));
        assertThat(last.getFin()).isEqualTo(LocalDateTime.of(2024, 2, 16, 17, 30));
        assertThat(last.getSemaineType()).isEqualTo(fr.manaken.plannif.model.SemaineType.SEMAINE_1);
    }

    private Planning generateProblem(String fichier) {
        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper()
                .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
                .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try (java.io.InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream(fichier)) {
            Planning planning = objectMapper.readValue(inputStream, Planning.class);
            
            Map<Long, fr.manaken.plannif.model.Classe> classeMap = planning.getClasses().stream().collect(Collectors.toMap(fr.manaken.plannif.model.Classe::getId, c -> c));
            Map<Long, fr.manaken.plannif.model.Matiere> matiereMap = planning.getMatieres().stream().collect(Collectors.toMap(fr.manaken.plannif.model.Matiere::getId, m -> m));

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

                com.fasterxml.jackson.databind.JsonNode presenceNode = rootNode.get("classePresences");
                if (presenceNode != null && presenceNode.isArray()) {
                    for (com.fasterxml.jackson.databind.JsonNode pn : presenceNode) {
                        Long id = pn.get("id").asLong();
                        Long classeId = pn.get("classe_id").asLong();
                        planning.getClassePresences().stream()
                                .filter(cp -> cp.getId().equals(id))
                                .findFirst()
                                .ifPresent(cp -> {
                                    planning.getClasses().stream()
                                            .filter(c -> c.getId().equals(classeId))
                                            .findFirst()
                                            .ifPresent(cp::setClasse);
                                });
                    }
                }

                com.fasterxml.jackson.databind.JsonNode dayOffNode = rootNode.get("professeurDayOffs");
                if (dayOffNode != null && dayOffNode.isArray()) {
                    for (com.fasterxml.jackson.databind.JsonNode don : dayOffNode) {
                        Long id = don.get("id").asLong();
                        Long profId = don.get("professeur_id").asLong();
                        planning.getProfesseurDayOffs().stream()
                                .filter(doff -> doff.getId().equals(id))
                                .findFirst()
                                .ifPresent(doff -> {
                                    planning.getProfesseurs().stream()
                                            .filter(p -> p.getId().equals(profId))
                                            .findFirst()
                                            .ifPresent(doff::setProfesseur);
                                });
                    }
                }

                com.fasterxml.jackson.databind.JsonNode configsNode = rootNode.get("matiereClasseConfigs");
                if (configsNode != null && configsNode.isArray()) {
                    for (com.fasterxml.jackson.databind.JsonNode cn : configsNode) {
                        MatiereClasseConfig config = objectMapper.treeToValue(cn, MatiereClasseConfig.class);
                        Long classeId = cn.get("classe_id").asLong();
                        config.setClasse(classeMap.get(classeId));
                        Long matiereId = cn.get("matiere_id").asLong();
                        config.setMatiere(matiereMap.get(matiereId));
                        planning.getMatiereClasseConfigs().add(config);
                    }
                }
            }

            // Populate semaineType on creneaux based on presence periods for backward compatibility in tests
            for (fr.manaken.plannif.model.Creneau c : planning.getCreneaux()) {
                if (c.getSemaineType() == null) {
                    for (fr.manaken.plannif.model.ClassePresence cp : planning.getClassePresences()) {
                        java.time.LocalDate d = c.getDebut().toLocalDate();
                        if (!d.isBefore(cp.getDateDebut()) && !d.isAfter(cp.getDateFin())) {
                            long days = java.time.temporal.ChronoUnit.DAYS.between(cp.getDateDebut(), d);
                            int weekIndex = (int) (days / 7) + 1;
                            c.setSemaineType(fr.manaken.plannif.model.SemaineType.fromIndex(weekIndex));
                            break;
                        }
                    }
                }
            }

            return planning;
        } catch (java.io.IOException e) {
            throw new RuntimeException("Erreur de chargement", e);
        }
    }

}
