package fr.manaken.plannif.mapper;

import fr.manaken.plannif.dto.MatiereDTO;
import fr.manaken.plannif.model.Matiere;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MatiereMapper {
    MatiereDTO toDto(Matiere entity);
}
