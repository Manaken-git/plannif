package fr.manaken.plannif.mapper;

import fr.manaken.plannif.dto.ClasseDTO;
import fr.manaken.plannif.model.Classe;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ClasseMapper {
    ClasseDTO toDto(Classe entity);
}
