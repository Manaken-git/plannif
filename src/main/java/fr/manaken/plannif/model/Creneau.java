package fr.manaken.plannif.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Creneau {

    @EqualsAndHashCode.Include
    private Long id;

    private LocalTime debut;
    private LocalTime fin;
}
