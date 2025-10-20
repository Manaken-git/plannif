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
    SeanceDTO toDto(Seance seance);

}
