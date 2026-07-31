package fr.manaken.plannif.model;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatiereClasseConfig {

    
    
    @EqualsAndHashCode.Include
    private Long id;

    
    
    private Classe classe;

    
    
    private Matiere matiere;

    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Long volumeHorairePeriode;
}
