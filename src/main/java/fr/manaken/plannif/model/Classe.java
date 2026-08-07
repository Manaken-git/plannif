package fr.manaken.plannif.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Classe {

    
    
    @EqualsAndHashCode.Include
    private Long id;

    private String nom;

    
    @JsonIgnore
    private Set<Seance> seances = new HashSet<>();

    
    @JsonIgnore
    private Set<Eleve> eleves = new HashSet<>();

    
    private java.util.List<ClassePresence> presences = new java.util.ArrayList<>();

    public boolean needsVieDeClasse(ClassePresence presence, java.util.List<Vacances> allVacances) {
        java.time.LocalDate lastFriday = presence.getDateFin();
        while (lastFriday.getDayOfWeek() != java.time.DayOfWeek.FRIDAY) {
            lastFriday = lastFriday.minusDays(1);
        }
        
        if (lastFriday.isBefore(presence.getDateDebut())) {
            return false;
        }

        java.time.LocalDate dateDebutPresence = presence.getDateDebut();
        Vacances lastVacances = null;
        for (Vacances v : allVacances) {
            if (v.getDateFin().isBefore(dateDebutPresence)) {
                if (lastVacances == null || v.getDateFin().isAfter(lastVacances.getDateFin())) {
                    lastVacances = v;
                }
            }
        }

        java.time.LocalDate repriseDate = (lastVacances != null) ? lastVacances.getDateFin().plusDays(1) : dateDebutPresence;

        for (Seance s : seances) {
            if (s.getType() == Seance.TypeSeance.VIE_DE_CLASSE && s.getCreneau() != null) {
                java.time.LocalDate dateSeance = s.getCreneau().getDebut().toLocalDate();
                if (!dateSeance.isBefore(repriseDate) && !dateSeance.isAfter(lastFriday)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public String toString() {
        return nom;
    }
}
