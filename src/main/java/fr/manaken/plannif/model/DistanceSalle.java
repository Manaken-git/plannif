package fr.manaken.plannif.model;


import lombok.Getter;
import lombok.Setter;



@Getter
@Setter
public class DistanceSalle {
    
    
    private Long id;

    
    
    private Salle salle1;

    
    
    private Salle salle2;

    private Long distance;
}
