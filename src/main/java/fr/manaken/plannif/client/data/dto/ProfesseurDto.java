package fr.manaken.plannif.client.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfesseurDto {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private BigDecimal nb_heures;
    private BigDecimal maxHeuresParJour;
    private BigDecimal maxHeuresParSemaine;
    private BigDecimal maxHeuresParSeance;
    
    private java.util.Set<MatiereDto> matieres;
    private java.util.List<ProfesseurDayOffDto> daysOff;
}
