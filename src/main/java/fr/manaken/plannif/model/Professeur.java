package fr.manaken.plannif.model;


import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Professeur {

    
    
    @EqualsAndHashCode.Include
    private Long id;

    private String nom;
    private String prenom;
    private String email;
    private BigDecimal nb_heures;
    private BigDecimal maxHeuresParJour;
    private BigDecimal maxHeuresParSemaine;
    private BigDecimal maxHeuresParSeance;

    
    
    private PlageHoraire plageHorairePreferee; // Nouveau champ

    
    private Set<Seance> seances = new HashSet<>();

    
    private java.util.List<ProfesseurDayOff> daysOff = new java.util.ArrayList<>();

    
    @JoinTable(
        name = "tj_professeur_matiere",
        joinColumns = ,
        inverseJoinColumns = 
    )
    private Set<Matiere> matieres = new HashSet<>();
}
