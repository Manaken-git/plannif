package fr.manaken.plannif.model;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class Eleve {

    
    
    private Long id;

    private String nom;
    private String prenom;

    
    
    private Classe classe;
}
