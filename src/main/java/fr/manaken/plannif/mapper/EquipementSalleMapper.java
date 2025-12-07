package fr.manaken.plannif.mapper;

import fr.manaken.plannif.dto.EquipementSalleDTO;
import fr.manaken.plannif.model.EquipementSalle;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EquipementSalleMapper {
    EquipementSalleDTO toDto(EquipementSalle entity);
}
