package fr.manaken.plannif.model;


import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Classe {

    
    
    @EqualsAndHashCode.Include
    private Long id;

    private String nom;

    
    private Set<Seance> seances = new HashSet<>();

    
    private Set<Eleve> eleves = new HashSet<>();

    
    private java.util.List<ClassePresence> presences = new java.util.ArrayList<>();
}
