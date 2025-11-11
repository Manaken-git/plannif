package fr.manaken.plannif.mapper;

import fr.manaken.plannif.dto.EquipementDTO;
import fr.manaken.plannif.model.Equipement;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EquipementMapper {
    EquipementDTO toDto(Equipement entity);
    Equipement toEntity(EquipementDTO dto);
}
