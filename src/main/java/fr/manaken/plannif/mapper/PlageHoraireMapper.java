package fr.manaken.plannif.mapper;

import fr.manaken.plannif.dto.PlageHoraireDTO;
import fr.manaken.plannif.model.PlageHoraire;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PlageHoraireMapper {
    PlageHoraireDTO toDto(PlageHoraire entity);
    PlageHoraire toEntity(PlageHoraireDTO dto);
}
