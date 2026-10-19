package fr.manaken.plannif.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ClasseReferent {

    @EqualsAndHashCode.Include
    private Long id;

    @JsonIgnore
    private Classe classe;

    private Professeur professeur;

    private RoleReferent role = RoleReferent.PRINCIPAL;
}
