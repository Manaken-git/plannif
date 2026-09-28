package fr.manaken.plannif.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

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

    @JsonIgnore
    private List<Vacances> vacances = new ArrayList<>();

    @JsonIgnore
    private List<LocalDateTime> availableDatesDebut = new ArrayList<>();

    @JsonIgnore
    private LocalDate cachedFirstDay;

    @JsonIgnore
    private LocalDate cachedLastDay;

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
        resetCache();
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
        resetCache();
    }

    public void setVacances(List<Vacances> vacances) {
        this.vacances = vacances;
        resetCache();
    }

    public void setAvailableDatesDebut(List<LocalDateTime> availableDatesDebut) {
        this.availableDatesDebut = availableDatesDebut;
        resetCache();
    }

    public void resetCache() {
        this.cachedFirstDay = null;
        this.cachedLastDay = null;
    }

    public LocalDate getFirstDay() {
        if (cachedFirstDay != null) {
            return cachedFirstDay;
        }
        if (dateDebut == null || dateFin == null) {
            return null;
        }
        LocalDate date = dateDebut;
        while (!date.isAfter(dateFin)) {
            if (isWorkingSchoolDay(date) && hasSlotAt(date, 9, 10)) {
                cachedFirstDay = date;
                return date;
            }
            date = date.plusDays(1);
        }
        return null;
    }

    public LocalDate getLastDay() {
        if (cachedLastDay != null) {
            return cachedLastDay;
        }
        if (dateDebut == null || dateFin == null) {
            return null;
        }
        LocalDate date = dateFin;
        while (!date.isBefore(dateDebut)) {
            if (isWorkingSchoolDay(date) && hasSlotAt(date, 10, 11)) {
                cachedLastDay = date;
                return date;
            }
            date = date.minusDays(1);
        }
        return null;
    }

    private boolean isWorkingSchoolDay(LocalDate date) {
        if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return false;
        }
        if (vacances != null) {
            for (Vacances v : vacances) {
                if (v.getDateDebut() != null && v.getDateFin() != null) {
                    if (!date.isBefore(v.getDateDebut()) && !date.isAfter(v.getDateFin())) {
                        return false;
                    }
                }
            }
        }
        if (availableDatesDebut != null && !availableDatesDebut.isEmpty()) {
            boolean hasAnySlot = false;
            for (LocalDateTime dt : availableDatesDebut) {
                if (dt != null && dt.toLocalDate().equals(date)) {
                    hasAnySlot = true;
                    break;
                }
            }
            if (!hasAnySlot) {
                return false;
            }
        }
        return true;
    }

    private boolean hasSlotAt(LocalDate date, int startHour, int endHour) {
        if (availableDatesDebut == null || availableDatesDebut.isEmpty()) {
            return true;
        }
        LocalDateTime target = date.atTime(startHour, 0);
        return availableDatesDebut.contains(target);
    }

    public boolean isValidVieDeClasse(LocalDateTime debut, LocalDateTime fin) {
        if (debut == null || fin == null) {
            return false;
        }
        LocalDate date = debut.toLocalDate();
        LocalTime start = debut.toLocalTime();
        LocalTime end = fin.toLocalTime();

        LocalDate firstDay = getFirstDay();
        LocalDate lastDay = getLastDay();
        if (firstDay != null && date.equals(firstDay)
                && start.equals(LocalTime.of(9, 0)) && end.equals(LocalTime.of(10, 0))) {
            return true;
        }

        return lastDay != null && date.equals(lastDay)
                && start.equals(LocalTime.of(10, 0)) && end.equals(LocalTime.of(11, 0));
    }
}
