package fr.manaken.plannif.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
public class Professeur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String prenom;
    private String email;
    private BigDecimal nb_heures;

    @ManyToOne
    @JoinColumn(name = "plage_horaire_preferee_id", referencedColumnName = "id")
    private PlageHoraire plageHorairePreferee; // Nouveau champ


    @OneToMany(mappedBy = "professeur")
    private Set<Seance> seances = new HashSet<>();
}
