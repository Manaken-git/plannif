package fr.manaken.plannif.mapper;

import fr.manaken.plannif.dto.SeanceDTO;
import fr.manaken.plannif.model.Seance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SeanceMapper {

    @Mapping(target = "professeurNomComplet", expression = "java(seance.getProfesseur().getPrenom() + \" \" + seance.getProfesseur().getNom())")
    @Mapping(target = "classeNom", source = "classe.nom")
    @Mapping(target = "matiereNom", source = "matiere.nom")
    @Mapping(target = "salleCode", source = "salle.code")
    @Mapping(target = "debut", source = "creneau.debut")
    @Mapping(target = "fin", source = "creneau.fin")
    SeanceDTO toDto(Seance seance);

    @Mapping(target = "professeur", ignore = true)
    @Mapping(target = "classe", ignore = true)
    @Mapping(target = "matiere", ignore = true)
    @Mapping(target = "salle", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "creneau.debut", source = "debut")
    @Mapping(target = "creneau.fin", source = "fin")
    Seance toEntity(SeanceDTO seanceDTO);

}
