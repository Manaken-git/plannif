package fr.manaken.plannif.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tj_equipements_salle")
@Getter
@Setter
public class EquipementSalle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "fk_salle_id", referencedColumnName = "id")
    private Salle salle;

    @ManyToOne
    @JoinColumn(name = "fk_equipement_id", referencedColumnName = "id")
    private Equipement equipement;
}
