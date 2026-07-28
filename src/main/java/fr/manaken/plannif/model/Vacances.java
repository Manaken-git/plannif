package fr.manaken.plannif.model;

import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Vacances {

    @EqualsAndHashCode.Include
    private Long id;

    private String nom;
    private LocalDate dateDebut;
    private LocalDate dateFin;
}
