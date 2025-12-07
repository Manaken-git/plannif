package fr.manaken.plannif.mapper;

import fr.manaken.plannif.dto.EleveDTO;
import fr.manaken.plannif.model.Eleve;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EleveMapper {

    EleveDTO toDto(Eleve eleve);
}
