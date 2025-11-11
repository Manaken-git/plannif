package fr.manaken.plannif.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tj_distance_salle")
@Getter
@Setter
public class DistanceSalle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "fk_salle1_id", referencedColumnName = "id")
    private Salle salle1;

    @ManyToOne
    @JoinColumn(name = "fk_salle2_id", referencedColumnName = "id")
    private Salle salle2;

    private Long distance;
}
