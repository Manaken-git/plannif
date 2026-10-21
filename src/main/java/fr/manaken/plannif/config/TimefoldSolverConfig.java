package fr.manaken.plannif.config;

import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.core.config.solver.SolverManagerConfig;
import ai.timefold.solver.core.config.solver.termination.TerminationConfig;
import fr.manaken.plannif.business.Planning;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class TimefoldSolverConfig {

    @Bean
    public SolverFactory<Planning> solverFactory(
            @Value("${planning.solver.spent-limit-seconds:30}") long solverSpentLimitSeconds,
            @Value("${planning.solver.unimproved-spent-limit-seconds:8}") long unimprovedSpentLimitSeconds,
            @Value("${planning.solver.environment-mode:NON_REPRODUCIBLE}") String environmentMode) {
        SolverConfig solverConfig = SolverConfig.createFromXmlResource("solverConfig.xml");
        try {
            solverConfig.setEnvironmentMode(ai.timefold.solver.core.config.solver.EnvironmentMode.valueOf(environmentMode));
        } catch (Exception e) {
            // Keep default from XML
        }
        TerminationConfig terminationConfig = solverConfig.getTerminationConfig();
        if (terminationConfig == null) {
            terminationConfig = new TerminationConfig();
            solverConfig.setTerminationConfig(terminationConfig);
        }
        terminationConfig.setSecondsSpentLimit(null);
        terminationConfig.setSpentLimit(Duration.ofSeconds(solverSpentLimitSeconds));
        if (unimprovedSpentLimitSeconds > 0) {
            terminationConfig.setUnimprovedSecondsSpentLimit(null);
            terminationConfig.setUnimprovedSpentLimit(Duration.ofSeconds(unimprovedSpentLimitSeconds));
        }
        return SolverFactory.create(solverConfig);
    }

    @Bean
    public SolverManager<Planning, String> solverManager(SolverFactory<Planning> solverFactory) {
        return SolverManager.create(solverFactory, new SolverManagerConfig());
    }
}
