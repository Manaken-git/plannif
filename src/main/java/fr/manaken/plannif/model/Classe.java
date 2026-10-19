package fr.manaken.plannif.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

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

    private java.util.List<ClasseReferent> referents = new java.util.ArrayList<>();

    public boolean isProfesseurReferent(Professeur p) {
        if (p == null || referents == null || referents.isEmpty()) {
            return false;
        }
        return referents.stream()
                .anyMatch(r -> r.getProfesseur() != null && r.getProfesseur().getId() != null && r.getProfesseur().getId().equals(p.getId()));
    }

    public Professeur getProfesseurReferentPrincipal() {
        if (referents == null || referents.isEmpty()) {
            return null;
        }
        return referents.stream()
                .filter(r -> r.getRole() == RoleReferent.PRINCIPAL && r.getProfesseur() != null)
                .map(ClasseReferent::getProfesseur)
                .findFirst()
                .orElseGet(() -> referents.get(0).getProfesseur());
    }

    @Override
    public String toString() {
        return nom;
    }
}
