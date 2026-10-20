package fr.manaken.plannif.controller;

import fr.manaken.plannif.business.Planning;
import fr.manaken.plannif.service.job.JobStatus;
import fr.manaken.plannif.service.job.PlanningJobManager;
import fr.manaken.plannif.service.job.PlanningJobState;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/planning/jobs")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class PlanningJobController {

    private final PlanningJobManager jobManager;

    public PlanningJobController(PlanningJobManager jobManager) {
        this.jobManager = jobManager;
    }

    public record JobSubmissionRequest(
            Planning planning,
            Map<String, String> constraintWeightOverrides
    ) {}

    @PostMapping
    public ResponseEntity<Map<String, Object>> submitJob(@RequestBody(required = false) JobSubmissionRequest request) {
        Planning customPlanning = request != null ? request.planning() : null;
        Map<String, String> overrides = request != null ? request.constraintWeightOverrides() : null;

        PlanningJobState state = jobManager.submitJob(customPlanning, overrides);

        Map<String, Object> response = new HashMap<>();
        response.put("jobId", state.getJobId());
        response.put("status", state.getStatus().name());
        response.put("submittedAt", state.getSubmittedAt());
        response.put("solverSpentLimitSeconds", state.getSolverSpentLimitSeconds());

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<Map<String, Object>> getJobStatus(@PathVariable String jobId) {
        return jobManager.getJob(jobId)
                .map(state -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("jobId", state.getJobId());
                    response.put("status", state.getStatus().name());
                    response.put("submittedAt", state.getSubmittedAt());
                    response.put("completedAt", state.getCompletedAt());
                    response.put("savedPlanningId", state.getSavedPlanningId());
                    response.put("score", state.getScore() != null ? state.getScore().toString() : null);
                    if (state.getScore() != null) {
                        response.put("hardScore", state.getScore().hardScore());
                        response.put("softScore", state.getScore().softScore());
                    }
                    response.put("errorMessage", state.getErrorMessage());
                    response.put("explanation", state.getExplanation());
                    if (state.getSubmittedAt() != null) {
                        LocalDateTime end = state.getCompletedAt() != null ? state.getCompletedAt() : LocalDateTime.now();
                        response.put("elapsedSeconds", Duration.between(state.getSubmittedAt(), end).toSeconds());
                    }
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/{jobId}/progress", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> streamProgress(@PathVariable String jobId) {
        if (jobManager.getJob(jobId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(jobManager.subscribeProgress(jobId));
    }

    @PostMapping("/{jobId}/stop")
    public ResponseEntity<Map<String, Object>> stopJob(@PathVariable String jobId) {
        var opt = jobManager.getJob(jobId);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        PlanningJobState state = opt.get();
        if (state.getStatus() != JobStatus.SOLVING) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "jobId", jobId,
                    "status", state.getStatus().name(),
                    "message", "Job is not actively solving."
            ));
        }

        jobManager.stopJob(jobId);

        return ResponseEntity.ok(Map.of(
                "jobId", jobId,
                "status", JobStatus.TERMINATED_EARLY.name(),
                "message", "Solver job terminated manually. Best solution saved."
        ));
    }
}
