package fr.manaken.plannif.model;








import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;

@Getter
@Setter


@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProfesseurDayOff {

    
    
    @EqualsAndHashCode.Include
    private Long id;

    
    
    @JsonIgnore
    private Professeur professeur;

    /**
     * 0 = Monday, 1 = Tuesday, ..., 4 = Friday
     */
    private Integer dayOfWeek;
}
