package fr.manaken.plannif.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter

public class Matiere {

    
    
    private Long id;

    private String nom;

    
    @JsonIgnore
    private Set<Seance> seances = new HashSet<>();
}
