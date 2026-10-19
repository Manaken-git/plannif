package fr.manaken.plannif.client.data.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.manaken.plannif.model.RoleReferent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClasseReferentDto {
    private Long id;
    private Long professeurId;
    private String professeurNom;
    private String professeurPrenom;
    private RoleReferent role;
}
