package fr.manaken.plannif.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Salle {

    
    
    @EqualsAndHashCode.Include
    private Long id;

    private String code; // ex: B203
    private Integer capacite;

    private String type; // Nouveau champ

    
    @JsonIgnore
    private Set<Seance> seances = new HashSet<>();

    @Override
    public String toString() {
        return code;
    }
}
