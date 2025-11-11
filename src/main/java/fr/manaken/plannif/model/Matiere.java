package fr.manaken.plannif.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
public class Matiere {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    private Long volumeHoraireAnnuel; // Nouveau champ

    @OneToMany(mappedBy = "matiere")
    private Set<Seance> seances = new HashSet<>();
}
