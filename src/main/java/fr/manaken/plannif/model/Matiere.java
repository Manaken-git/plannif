package fr.manaken.plannif.model;


import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter

public class Matiere {

    
    
    private Long id;

    private String nom;

    
    private Set<Seance> seances = new HashSet<>();
}
