package fr.manaken.plannif.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Classe {

    
    
    @EqualsAndHashCode.Include
    private Long id;

    private String nom;

    
    @JsonIgnore
    private Set<Seance> seances = new HashSet<>();

    
    @JsonIgnore
    private Set<Eleve> eleves = new HashSet<>();

    
    private java.util.List<ClassePresence> presences = new java.util.ArrayList<>();

    @Override
    public String toString() {
        return nom;
    }
}
