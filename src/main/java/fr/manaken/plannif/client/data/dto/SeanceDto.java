package fr.manaken.plannif.client.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeanceDto {
    private Long id;
    private Long professeurId;
    private Long classeId;
    private Long matiereId;
    private Long salleId;
    private Long creneauId;
    private String type;
    private java.time.LocalDateTime debut;
    private java.time.LocalDateTime fin;
}
