package fr.manaken.plannif.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ClassePresence {

    @EqualsAndHashCode.Include
    private Long id;

    @JsonIgnore
    private Classe classe;

    private LocalDate dateDebut;
    private LocalDate dateFin;

    public LocalDate getFirstMonday() {
        if (dateDebut == null || dateFin == null) {
            return null;
        }
        LocalDate date = dateDebut;
        while (date.getDayOfWeek() != DayOfWeek.MONDAY && !date.isAfter(dateFin)) {
            date = date.plusDays(1);
        }
        return date.isAfter(dateFin) ? null : date;
    }

    public LocalDate getLastFriday() {
        if (dateDebut == null || dateFin == null) {
            return null;
        }
        LocalDate date = dateFin;
        while (date.getDayOfWeek() != DayOfWeek.FRIDAY && !date.isBefore(dateDebut)) {
            date = date.minusDays(1);
        }
        return date.isBefore(dateDebut) ? null : date;
    }

    public boolean isValidVieDeClasse(Creneau creneau) {
        if (creneau == null || creneau.getDebut() == null || creneau.getFin() == null) {
            return false;
        }
        LocalDate date = creneau.getDebut().toLocalDate();
        LocalTime start = creneau.getDebut().toLocalTime();
        LocalTime end = creneau.getFin().toLocalTime();

        LocalDate firstMonday = getFirstMonday();
        if (firstMonday != null && date.equals(firstMonday)
                && start.equals(LocalTime.of(9, 0)) && end.equals(LocalTime.of(10, 0))) {
            return true;
        }

        LocalDate lastFriday = getLastFriday();
        if (lastFriday != null && date.equals(lastFriday)
                && start.equals(LocalTime.of(10, 0)) && end.equals(LocalTime.of(11, 0))) {
            return true;
        }

        return false;
    }
}
