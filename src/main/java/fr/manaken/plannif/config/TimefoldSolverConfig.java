package fr.manaken.plannif.config;

import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.core.config.solver.SolverManagerConfig;
import fr.manaken.plannif.business.Planning;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class TimefoldSolverConfig {

    @Bean
    public SolverFactory<Planning> solverFactory(@Value("${planning.solver.spent-limit-seconds:120}") long solverSpentLimitSeconds) {
        SolverConfig solverConfig = SolverConfig.createFromXmlResource("solverConfig.xml")
                .withTerminationSpentLimit(Duration.ofSeconds(solverSpentLimitSeconds));
        return SolverFactory.create(solverConfig);
    }

    @Bean
    public SolverManager<Planning, String> solverManager(SolverFactory<Planning> solverFactory) {
        return SolverManager.create(solverFactory, new SolverManagerConfig());
    }
}
