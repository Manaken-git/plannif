package fr.manaken.plannif.client.data.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClasseDto {
    private Long id;
    private String nom;
    private List<ClassePresenceDto> presences;
    private List<ClasseReferentDto> referents;
}
