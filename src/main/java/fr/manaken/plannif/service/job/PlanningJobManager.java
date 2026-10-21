package fr.manaken.plannif.service.job;

import ai.timefold.solver.core.api.score.ScoreExplanation;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.api.solver.SolverManager;
import fr.manaken.plannif.business.Planning;
import fr.manaken.plannif.service.PlanningService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import java.util.stream.Collectors;

@Slf4j
@Service
public class PlanningJobManager {

    private final SolverManager<Planning, String> solverManager;
    private final SolverFactory<Planning> solverFactory;
    private final PlanningService planningService;
    private final long solverSpentLimitSeconds;

    private final Map<String, PlanningJobState> jobs = new ConcurrentHashMap<>();

    public PlanningJobManager(SolverManager<Planning, String> solverManager,
                              SolverFactory<Planning> solverFactory,
                              PlanningService planningService,
                              @Value("${planning.solver.spent-limit-seconds:30}") long solverSpentLimitSeconds) {
        this.solverManager = solverManager;
        this.solverFactory = solverFactory;
        this.planningService = planningService;
        this.solverSpentLimitSeconds = solverSpentLimitSeconds;
    }

    public PlanningJobState submitJob(Planning customProblem, Map<String, String> constraintWeightOverrides) {
        String jobId = UUID.randomUUID().toString();
        Planning problem;

        // If customProblem is already a complete problem with its candidate dates (e.g. in unit tests), use it directly
        if (customProblem != null && customProblem.getDatesDebutPossibles() != null && !customProblem.getDatesDebutPossibles().isEmpty()) {
            problem = customProblem;
        } else {
            // Otherwise, load full base problem from database
            Planning baseProblem = planningService.buildPlanning();
            if (baseProblem != null) {
                problem = baseProblem;
                if (customProblem != null && customProblem.getSeances() != null && !customProblem.getSeances().isEmpty()) {
                    Map<Long, fr.manaken.plannif.model.Seance> customSeanceMap = customProblem.getSeances().stream()
                            .filter(s -> s.getId() != null)
                            .collect(Collectors.toMap(fr.manaken.plannif.model.Seance::getId, s -> s, (a, b) -> a));

                    if (problem.getSeances() != null) {
                        for (fr.manaken.plannif.model.Seance baseSeance : problem.getSeances()) {
                            fr.manaken.plannif.model.Seance custom = customSeanceMap.get(baseSeance.getId());
                            if (custom != null) {
                                baseSeance.setPinned(custom.isPinned());
                                if (custom.isPinned()) {
                                    if (custom.getDebut() != null) {
                                        baseSeance.setDebut(custom.getDebut());
                                    }
                                    if (custom.getFin() != null) {
                                        baseSeance.setFin(custom.getFin());
                                    }
                                    if (custom.getSalle() != null) {
                                        baseSeance.setSalle(custom.getSalle());
                                    }
                                    if (custom.getProfesseur() != null) {
                                        baseSeance.setProfesseur(custom.getProfesseur());
                                    }
                                }
                            }
                        }
                    }
                }
                if (customProblem != null && customProblem.getNom() != null) {
                    problem.setNom(customProblem.getNom());
                }
            } else {
                problem = customProblem != null ? customProblem : new Planning();
            }
        }

        planningService.generateCreneauxIfNeeded(problem);
        planningService.generateSeancesIfNeeded(problem);

        Map<String, HardSoftScore> effectiveOverrides = new HashMap<>();
        effectiveOverrides.put(fr.manaken.plannif.business.PlanningConstraintConfiguration.STUDENT_GROUP_PRESENCE, HardSoftScore.ofHard(10));
        effectiveOverrides.put(fr.manaken.plannif.business.PlanningConstraintConfiguration.SUBJECT_CLASS_PERIOD, HardSoftScore.ofHard(10));
        effectiveOverrides.put(fr.manaken.plannif.business.PlanningConstraintConfiguration.TEACHER_MUST_BE_QUALIFIED, HardSoftScore.ofHard(10));
        effectiveOverrides.put(fr.manaken.plannif.business.PlanningConstraintConfiguration.ROOM_CONFLICT, HardSoftScore.ofHard(10));
        effectiveOverrides.put(fr.manaken.plannif.business.PlanningConstraintConfiguration.TEACHER_CONFLICT, HardSoftScore.ofHard(10));
        effectiveOverrides.put(fr.manaken.plannif.business.PlanningConstraintConfiguration.STUDENT_GROUP_CONFLICT, HardSoftScore.ofHard(10));
        effectiveOverrides.put(fr.manaken.plannif.business.PlanningConstraintConfiguration.HOLIDAY_CONFLICT, HardSoftScore.ofHard(10));

        if (constraintWeightOverrides != null && !constraintWeightOverrides.isEmpty()) {
            constraintWeightOverrides.forEach((name, scoreStr) -> effectiveOverrides.put(name, HardSoftScore.parseScore(scoreStr)));
        }
        problem.setConstraintWeightOverrides(ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides.of(effectiveOverrides));

        PlanningJobState state = PlanningJobState.builder()
                .jobId(jobId)
                .status(JobStatus.SOLVING)
                .submittedAt(LocalDateTime.now())
                .solverSpentLimitSeconds(solverSpentLimitSeconds)
                .build();
        jobs.put(jobId, state);

        solverManager.solveBuilder()
                .withProblemId(jobId)
                .withProblemFinder(id -> problem)
                .withBestSolutionConsumer(bestSolution -> onBestSolution(jobId, bestSolution))
                .withFinalBestSolutionConsumer(finalSolution -> onFinalSolution(jobId, finalSolution))
                .withExceptionHandler((id, throwable) -> onException(jobId, throwable))
                .run();

        return state;
    }

    private void onBestSolution(String jobId, Planning solution) {
        PlanningJobState state = jobs.get(jobId);
        if (state == null) return;
        state.setScore(solution.getScore());
        log.debug("Job {} new best score: {}", jobId, solution.getScore());

        try {
            SolutionManager<Planning, HardSoftScore> solutionManager = SolutionManager.create(solverFactory);
            ScoreExplanation<Planning, HardSoftScore> explanation = solutionManager.explain(solution);
            state.setExplanation(buildExplanationMap(explanation));
        } catch (Exception ignored) {}

        Map<String, Object> data = new HashMap<>();
        data.put("jobId", jobId);
        data.put("status", state.getStatus().name());
        data.put("score", solution.getScore() != null ? solution.getScore().toString() : "");
        data.put("hardScore", solution.getScore() != null ? solution.getScore().hardScore() : 0);
        data.put("softScore", solution.getScore() != null ? solution.getScore().softScore() : 0);
        data.put("elapsedSeconds", Duration.between(state.getSubmittedAt(), LocalDateTime.now()).toSeconds());

        sendSseEvent(state, "score-update", data);
    }

    private void onFinalSolution(String jobId, Planning solution) {
        PlanningJobState state = jobs.get(jobId);
        if (state == null) return;

        try {
            planningService.savePlanning(solution);
            state.setSavedPlanningId(solution.getId());
            log.info("Job {} completed and persisted with ID {}", jobId, solution.getId());
        } catch (Exception e) {
            log.error("Failed to persist planning for job {}", jobId, e);
        }

        if (state.getStatus() == JobStatus.SOLVING) {
            state.setStatus(JobStatus.COMPLETED);
        }
        state.setCompletedAt(LocalDateTime.now());
        state.setScore(solution.getScore());

        // Score explanation
        try {
            SolutionManager<Planning, HardSoftScore> solutionManager = SolutionManager.create(solverFactory);
            ScoreExplanation<Planning, HardSoftScore> explanation = solutionManager.explain(solution);
            state.setExplanation(buildExplanationMap(explanation));
            log.info("Job {} final score explanation:\n{}", jobId, explanation.getSummary());
        } catch (Exception e) {
            log.warn("Failed to generate score explanation for job {}", jobId, e);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("jobId", jobId);
        data.put("status", state.getStatus().name());
        data.put("finalScore", solution.getScore() != null ? solution.getScore().toString() : "");
        data.put("savedPlanningId", state.getSavedPlanningId());
        data.put("durationSeconds", Duration.between(state.getSubmittedAt(), state.getCompletedAt()).toSeconds());

        sendSseEvent(state, "completed", data);
        closeEmitters(state);
    }

    private void onException(String jobId, Throwable throwable) {
        PlanningJobState state = jobs.get(jobId);
        if (state == null) return;
        state.setStatus(JobStatus.FAILED);
        state.setCompletedAt(LocalDateTime.now());
        state.setErrorMessage(throwable.getMessage());
        log.error("Job {} failed with error", jobId, throwable);

        Map<String, Object> data = new HashMap<>();
        data.put("jobId", jobId);
        data.put("status", JobStatus.FAILED.name());
        data.put("error", throwable.getMessage());

        sendSseEvent(state, "error", data);
        closeEmitters(state);
    }

    public boolean stopJob(String jobId) {
        PlanningJobState state = jobs.get(jobId);
        if (state == null) return false;
        if (state.getStatus() == JobStatus.SOLVING) {
            state.setStatus(JobStatus.TERMINATED_EARLY);
            solverManager.terminateEarly(jobId);
            return true;
        }
        return false;
    }

    public Optional<PlanningJobState> getJob(String jobId) {
        return Optional.ofNullable(jobs.get(jobId));
    }

    public SseEmitter subscribeProgress(String jobId) {
        PlanningJobState state = jobs.get(jobId);
        if (state == null) {
            throw new NoSuchElementException("Job not found: " + jobId);
        }
        SseEmitter emitter = new SseEmitter((solverSpentLimitSeconds + 30) * 1000L);
        state.getEmitters().add(emitter);

        emitter.onCompletion(() -> state.getEmitters().remove(emitter));
        emitter.onTimeout(() -> state.getEmitters().remove(emitter));
        emitter.onError(e -> state.getEmitters().remove(emitter));

        try {
            Map<String, Object> initial = new HashMap<>();
            initial.put("jobId", jobId);
            initial.put("status", state.getStatus().name());
            initial.put("score", state.getScore() != null ? state.getScore().toString() : "");
            if (state.getScore() != null) {
                initial.put("hardScore", state.getScore().hardScore());
                initial.put("softScore", state.getScore().softScore());
            }
            if (state.getSubmittedAt() != null) {
                initial.put("elapsedSeconds", Duration.between(state.getSubmittedAt(), LocalDateTime.now()).toSeconds());
            }
            emitter.send(SseEmitter.event().name("status").data(initial));
        } catch (Exception e) {
            state.getEmitters().remove(emitter);
        }

        if (state.getStatus() == JobStatus.COMPLETED || state.getStatus() == JobStatus.TERMINATED_EARLY) {
            try {
                Map<String, Object> completedData = new HashMap<>();
                completedData.put("jobId", jobId);
                completedData.put("status", state.getStatus().name());
                completedData.put("finalScore", state.getScore() != null ? state.getScore().toString() : "");
                completedData.put("savedPlanningId", state.getSavedPlanningId());
                if (state.getSubmittedAt() != null && state.getCompletedAt() != null) {
                    completedData.put("durationSeconds", Duration.between(state.getSubmittedAt(), state.getCompletedAt()).toSeconds());
                }
                emitter.send(SseEmitter.event().name("completed").data(completedData));
            } catch (Exception ignored) {}
            emitter.complete();
        } else if (state.getStatus() == JobStatus.FAILED) {
            try {
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("jobId", jobId);
                errorData.put("status", JobStatus.FAILED.name());
                errorData.put("error", state.getErrorMessage());
                emitter.send(SseEmitter.event().name("error").data(errorData));
            } catch (Exception ignored) {}
            emitter.complete();
        }

        return emitter;
    }

    private void sendSseEvent(PlanningJobState state, String eventName, Object data) {
        for (SseEmitter emitter : state.getEmitters()) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (Exception e) {
                state.getEmitters().remove(emitter);
            }
        }
    }

    private void closeEmitters(PlanningJobState state) {
        for (SseEmitter emitter : state.getEmitters()) {
            try {
                emitter.complete();
            } catch (Exception ignored) {}
        }
        state.getEmitters().clear();
    }

    private Map<String, Object> buildExplanationMap(ScoreExplanation<Planning, HardSoftScore> explanation) {
        HardSoftScore score = explanation.getScore();
        List<Map<String, Object>> matches = new ArrayList<>();
        for (var entry : explanation.getConstraintMatchTotalMap().values()) {
            if (entry.getConstraintMatchCount() > 0) {
                Map<String, Object> match = new HashMap<>();
                match.put("constraintName", entry.getConstraintRef() != null ? entry.getConstraintRef().constraintName() : entry.getConstraintRef().constraintId());
                match.put("score", entry.getScore() != null ? entry.getScore().toString() : "");
                match.put("count", entry.getConstraintMatchCount());
                matches.add(match);
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("hardScore", score != null ? score.hardScore() : 0);
        result.put("softScore", score != null ? score.softScore() : 0);
        result.put("summary", explanation.getSummary());
        result.put("matches", matches);
        return result;
    }
}
