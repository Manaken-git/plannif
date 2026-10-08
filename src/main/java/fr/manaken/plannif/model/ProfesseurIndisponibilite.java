package fr.manaken.plannif.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfesseurIndisponibilite {
    private Long id;
    private Professeur professeur;
    private LocalDate date;
    private Integer dayOfWeek; // 0=Lundi..4=Vendredi
    private LocalTime heureDebut;
    private LocalTime heureFin;

    public boolean matches(LocalDateTime sessionDebut, LocalDateTime sessionFin) {
        if (sessionDebut == null || sessionFin == null) {
            return false;
        }
        if (date != null && !sessionDebut.toLocalDate().equals(date)) {
            return false;
        }
        if (dayOfWeek != null && (sessionDebut.getDayOfWeek().getValue() - 1) != dayOfWeek) {
            return false;
        }
        LocalTime debutTime = sessionDebut.toLocalTime();
        LocalTime finTime = sessionFin.toLocalTime();

        LocalTime start = heureDebut != null ? heureDebut : LocalTime.MIN;
        LocalTime end = heureFin != null ? heureFin : LocalTime.MAX;

        return debutTime.isBefore(end) && finTime.isAfter(start);
    }
}
