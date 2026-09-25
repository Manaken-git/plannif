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
        return countVieDeClasseNeeded(presence) > 0;
    }

    public int countVieDeClasseNeeded(ClassePresence presence) {
        if (presence == null) {
            return 0;
        }
        int needed = 0;
        if (presence.getFirstMonday() != null) {
            boolean hasMonday = seances != null && seances.stream().anyMatch(s ->
                    s.getType() == Seance.TypeSeance.VIE_DE_CLASSE
                            && s.getCreneau() != null
                            && presence.isValidVieDeClasse(s.getCreneau())
                            && s.getCreneau().getDebut().toLocalDate().equals(presence.getFirstMonday()));
            if (!hasMonday) {
                needed++;
            }
        }
        if (presence.getLastFriday() != null) {
            boolean hasFriday = seances != null && seances.stream().anyMatch(s ->
                    s.getType() == Seance.TypeSeance.VIE_DE_CLASSE
                            && s.getCreneau() != null
                            && presence.isValidVieDeClasse(s.getCreneau())
                            && s.getCreneau().getDebut().toLocalDate().equals(presence.getLastFriday()));
            if (!hasFriday) {
                needed++;
            }
        }
        return needed;
    }

    @Override
    public String toString() {
        return nom;
    }
}
