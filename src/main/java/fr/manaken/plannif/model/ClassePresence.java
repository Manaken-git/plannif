package fr.manaken.plannif.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Getter
@Setter


@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ClassePresence {

    
    
    @EqualsAndHashCode.Include
    private Long id;

    
    
    @JsonIgnore
    private Classe classe;

    
    private LocalDate dateDebut;

    
    private LocalDate dateFin;
}
