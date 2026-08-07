package fr.manaken.plannif.client.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanningDto {
    private Long id;
    private String nom;
    private LocalDateTime dateCreation;
    private List<SeanceDto> seances;
    private List<CreneauDTO> creneaux;
}
