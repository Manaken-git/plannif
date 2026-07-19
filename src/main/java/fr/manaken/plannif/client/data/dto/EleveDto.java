package fr.manaken.plannif.client.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EleveDto {
    private Long id;
    private String nom;
    private String prenom;
    private Long classeId;
}
