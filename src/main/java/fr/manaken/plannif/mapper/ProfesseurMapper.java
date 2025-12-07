package fr.manaken.plannif.mapper;

import fr.manaken.plannif.dto.ProfesseurDTO;
import fr.manaken.plannif.model.Professeur;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = { SeanceMapper.class })
public interface ProfesseurMapper {

    ProfesseurDTO toDto(Professeur professeur);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void mergeWDTO(@MappingTarget Professeur pBDD, ProfesseurDTO pDTO);
}
