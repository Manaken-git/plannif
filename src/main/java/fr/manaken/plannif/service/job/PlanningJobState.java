package fr.manaken.plannif.service.job;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Data
@Builder
public class PlanningJobState {
    private String jobId;
    private JobStatus status;
    private LocalDateTime submittedAt;
    private LocalDateTime completedAt;
    private Long solverSpentLimitSeconds;
    private HardSoftScore score;
    private Long savedPlanningId;
    private String errorMessage;
    private Map<String, Object> explanation;

    @Builder.Default
    private List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
}
