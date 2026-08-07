package fr.manaken.plannif.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Creneau {

    @EqualsAndHashCode.Include
    private Long id;

    private LocalDateTime debut;
    private LocalDateTime fin;
    private SemaineType semaineType;

    @Override
    public String toString() {
        if (debut == null || fin == null) {
            return "null";
        }
        return debut.getDayOfWeek() + " " + debut.toLocalTime() + "-" + fin.toLocalTime() + (semaineType != null ? " (" + semaineType + ")" : "");
    }
}

