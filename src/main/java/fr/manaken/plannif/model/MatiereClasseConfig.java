package fr.manaken.plannif.model;


import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;

@Getter
@Setter


@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatiereClasseConfig {

    
    
    private Long id;

    
    
    private Classe classe;

    
    
    private Matiere matiere;

    private LocalDate dateDebut;
    private LocalDate dateFin;
}
