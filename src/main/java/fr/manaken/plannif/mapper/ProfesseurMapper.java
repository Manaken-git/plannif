package fr.manaken.plannif.mapper;

import fr.manaken.plannif.dto.ProfesseurDTO;
import fr.manaken.plannif.model.Professeur;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProfesseurMapper {


    ProfesseurDTO toDto(Professeur professeur);
    Professeur toEntity(ProfesseurDTO professeurDTO);
}
