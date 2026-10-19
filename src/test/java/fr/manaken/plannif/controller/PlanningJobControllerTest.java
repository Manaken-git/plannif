package fr.manaken.plannif.controller;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.core.config.solver.SolverManagerConfig;
import fr.manaken.plannif.business.Planning;
import fr.manaken.plannif.business.PlanningConstraintConfiguration;
import fr.manaken.plannif.business.PlanningConstraints;
import fr.manaken.plannif.model.Classe;
import fr.manaken.plannif.model.Matiere;
import fr.manaken.plannif.model.Professeur;
import fr.manaken.plannif.model.Salle;
import fr.manaken.plannif.model.Seance;
import fr.manaken.plannif.service.PlanningService;
import fr.manaken.plannif.service.job.JobStatus;
import fr.manaken.plannif.service.job.PlanningJobManager;
import fr.manaken.plannif.service.job.PlanningJobState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

@ExtendWith(MockitoExtension.class)
class PlanningJobControllerTest {

    @Mock
    private PlanningService planningService;

    private PlanningJobManager jobManager;
    private PlanningJobController controller;
    private SolverManager<Planning, String> solverManager;
    private SolverFactory<Planning> solverFactory;

    @BeforeEach
    void setUp() {
        SolverConfig solverConfig = new SolverConfig()
                .withSolutionClass(Planning.class)
                .withEntityClasses(Seance.class)
                .withConstraintProviderClass(PlanningConstraints.class)
                .withTerminationSpentLimit(Duration.ofSeconds(2));

        solverFactory = SolverFactory.create(solverConfig);
        solverManager = SolverManager.create(solverFactory, new SolverManagerConfig());

        jobManager = new PlanningJobManager(solverManager, solverFactory, planningService, 10);
        controller = new PlanningJobController(jobManager);
    }

    private Planning createSamplePlanning() {
        Planning planning = new Planning();
        LocalDateTime dt = LocalDateTime.of(2024, 2, 12, 9, 0);
        planning.getDatesDebutPossibles().add(dt);

        Salle salle = new Salle();
        salle.setId(1L);
        salle.setCode("A101");
        salle.setCapacite(30);
        planning.getSalles().add(salle);

        Professeur prof = new Professeur();
        prof.setId(1L);
        prof.setNom("Dupont");
        planning.getProfesseurs().add(prof);

        Classe classe = new Classe();
        classe.setId(1L);
        classe.setNom("6A");
        planning.getClasses().add(classe);

        Matiere matiere = new Matiere();
        matiere.setId(1L);
        matiere.setNom("Maths");
        planning.getMatieres().add(matiere);

        Seance seance = new Seance();
        seance.setId(1L);
        seance.setClasse(classe);
        seance.setMatiere(matiere);
        seance.setType(Seance.TypeSeance.COURS);
        planning.getSeances().add(seance);

        return planning;
    }

    @Test
    void testSubmitJobReturnsAcceptedImmediately() {
        Planning sample = createSamplePlanning();
        PlanningJobController.JobSubmissionRequest request = new PlanningJobController.JobSubmissionRequest(sample, null);

        long start = System.currentTimeMillis();
        ResponseEntity<Map<String, Object>> response = controller.submitJob(request);
        long duration = System.currentTimeMillis() - start;

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(duration).isLessThan(500); // Doit retourner immédiatement

        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("jobId")).isNotNull();
        assertThat(body.get("status")).isEqualTo("SOLVING");
        assertThat(body.get("submittedAt")).isNotNull();
    }

    @Test
    void testGetJobStatus() {
        Planning sample = createSamplePlanning();
        var submitResponse = controller.submitJob(new PlanningJobController.JobSubmissionRequest(sample, null));
        String jobId = (String) submitResponse.getBody().get("jobId");

        ResponseEntity<Map<String, Object>> statusResponse = controller.getJobStatus(jobId);
        assertThat(statusResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> body = statusResponse.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("jobId")).isEqualTo(jobId);
        assertThat(body.get("status")).isNotNull();
        assertThat(body.get("submittedAt")).isNotNull();
        assertThat(body.get("elapsedSeconds")).isNotNull();
    }

    @Test
    void testGetJobStatusNotFound() {
        ResponseEntity<Map<String, Object>> statusResponse = controller.getJobStatus("unknown-job-id");
        assertThat(statusResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testStreamProgressReturnsEmitter() {
        Planning sample = createSamplePlanning();
        var submitResponse = controller.submitJob(new PlanningJobController.JobSubmissionRequest(sample, null));
        String jobId = (String) submitResponse.getBody().get("jobId");

        ResponseEntity<SseEmitter> response = controller.streamProgress(jobId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void testStreamProgressReturnsNotFoundForUnknownJob() {
        ResponseEntity<SseEmitter> response = controller.streamProgress("unknown-job-id");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testStreamProgressOnCompletedJob() {
        Planning sample = createSamplePlanning();
        var submitResponse = controller.submitJob(new PlanningJobController.JobSubmissionRequest(sample, null));
        String jobId = (String) submitResponse.getBody().get("jobId");

        // Force stop to set completed / terminated status
        controller.stopJob(jobId);

        ResponseEntity<SseEmitter> response = controller.streamProgress(jobId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void testStopJob() {
        Planning sample = createSamplePlanning();
        var submitResponse = controller.submitJob(new PlanningJobController.JobSubmissionRequest(sample, null));
        String jobId = (String) submitResponse.getBody().get("jobId");

        ResponseEntity<Map<String, Object>> stopResponse = controller.stopJob(jobId);
        assertThat(stopResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(stopResponse.getBody().get("status")).isEqualTo(JobStatus.TERMINATED_EARLY.name());
    }

    @Test
    void testStopJobNotFound() {
        ResponseEntity<Map<String, Object>> stopResponse = controller.stopJob("unknown-job-id");
        assertThat(stopResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testStopJobConflictWhenAlreadyStopped() {
        Planning sample = createSamplePlanning();
        var submitResponse = controller.submitJob(new PlanningJobController.JobSubmissionRequest(sample, null));
        String jobId = (String) submitResponse.getBody().get("jobId");

        ResponseEntity<Map<String, Object>> stop1 = controller.stopJob(jobId);
        assertThat(stop1.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Map<String, Object>> stop2 = controller.stopJob(jobId);
        assertThat(stop2.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void testSubmitJobWithConstraintWeightOverrides() {
        Planning sample = createSamplePlanning();
        Map<String, String> overrides = Map.of(PlanningConstraintConfiguration.ROOM_CONFLICT, "10hard/0soft");
        var request = new PlanningJobController.JobSubmissionRequest(sample, overrides);

        ResponseEntity<Map<String, Object>> response = controller.submitJob(request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        String jobId = (String) response.getBody().get("jobId");
        assertThat(jobId).isNotNull();

        assertThat(jobManager.getJob(jobId)).isPresent();
    }
}
