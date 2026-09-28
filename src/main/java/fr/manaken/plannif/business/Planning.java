package fr.manaken.plannif.business;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.manaken.plannif.model.Salle;
import fr.manaken.plannif.model.Seance;
import fr.manaken.plannif.model.Professeur;
import fr.manaken.plannif.model.Classe;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@PlanningSolution
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Planning {

    private Long id;
    private String nom;
    private LocalDateTime dateCreation;

    @PlanningEntityCollectionProperty
    private List<Seance> seances = new java.util.ArrayList<>();

    @ValueRangeProvider(id = "dateDebutRange")
    @ProblemFactCollectionProperty
    private List<LocalDateTime> datesDebutPossibles = new java.util.ArrayList<>();

    @ValueRangeProvider(id = "salleRange")
    @ProblemFactCollectionProperty
    private List<Salle> salles = new java.util.ArrayList<>();

    @ValueRangeProvider(id = "professeurRange")
    @ProblemFactCollectionProperty
    private List<Professeur> professeurs = new java.util.ArrayList<>();

    @ProblemFactCollectionProperty
    private List<Classe> classes = new java.util.ArrayList<>();

    @ProblemFactCollectionProperty
    private List<fr.manaken.plannif.model.Matiere> matieres = new java.util.ArrayList<>();

    @ProblemFactCollectionProperty
    private List<fr.manaken.plannif.model.ProfesseurDayOff> professeurDayOffs = new java.util.ArrayList<>();

    @ProblemFactCollectionProperty
    private List<fr.manaken.plannif.model.ClassePresence> classePresences = new java.util.ArrayList<>();

    @ProblemFactCollectionProperty
    private List<fr.manaken.plannif.model.MatiereClasseConfig> matiereClasseConfigs = new java.util.ArrayList<>();

    @ProblemFactCollectionProperty
    private List<fr.manaken.plannif.model.Vacances> vacances = new java.util.ArrayList<>();

    @PlanningScore
    private HardSoftScore score;

    @JsonProperty("creneaux")
    public void setCreneauxFromJson(List<Map<String, Object>> creneauxJson) {
        if (creneauxJson != null) {
            for (Map<String, Object> map : creneauxJson) {
                Object debutObj = map.get("debut");
                if (debutObj instanceof String str) {
                    try {
                        LocalDateTime dt = LocalDateTime.parse(str);
                        if (!this.datesDebutPossibles.contains(dt)) {
                            this.datesDebutPossibles.add(dt);
                        }
                    } catch (Exception ignored) {}
                }
            }
        }
    }
}