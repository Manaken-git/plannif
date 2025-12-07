package fr.manaken.plannif.mapper;

import fr.manaken.plannif.dto.SalleDTO;
import fr.manaken.plannif.model.Salle;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SalleMapper {
    SalleDTO toDto(Salle entity);
}
