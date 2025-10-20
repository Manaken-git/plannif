package fr.manaken.plannif.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class Seance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime debut;
    private LocalDateTime fin;

    @ManyToOne
    @JoinColumn(name = "professeur_id")
    private Professeur professeur;

    @ManyToOne
    @JoinColumn(name = "classe_id")
    private Classe classe;

    @ManyToOne
    @JoinColumn(name = "matiere_id")
    private Matiere matiere;

    @ManyToOne
    @JoinColumn(name = "salle_id")
    private Salle salle;

}
