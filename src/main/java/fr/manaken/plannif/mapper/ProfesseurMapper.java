package fr.manaken.plannif.mapper;

import fr.manaken.plannif.dto.ProfesseurDTO;
import fr.manaken.plannif.model.Professeur;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ProfesseurMapper {

    ProfesseurDTO toDto(Professeur professeur);
    Professeur toEntity(ProfesseurDTO professeurDTO);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void mergeWDTO(@MappingTarget Professeur pBDD, ProfesseurDTO pDTO);
}
