package fr.manaken.plannif.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
public class Salle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code; // ex: B203
    private Integer capacite;

    private String type; // Nouveau champ

    @OneToMany(mappedBy = "salle")
    private Set<Seance> seances = new HashSet<>();
}
