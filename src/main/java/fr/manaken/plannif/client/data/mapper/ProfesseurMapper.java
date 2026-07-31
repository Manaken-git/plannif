package fr.manaken.plannif.client.data.mapper;

import fr.manaken.plannif.client.data.dto.ProfesseurDayOffDto;
import fr.manaken.plannif.client.data.dto.ProfesseurDto;
import fr.manaken.plannif.model.Professeur;
import fr.manaken.plannif.model.ProfesseurDayOff;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {MatiereMapper.class})
public interface ProfesseurMapper {
    ProfesseurDto toDto(Professeur entity);
    Professeur toEntity(ProfesseurDto dto);
    List<ProfesseurDto> toDtoList(List<Professeur> entities);
    List<Professeur> toEntityList(List<ProfesseurDto> dtos);

    ProfesseurDayOff toEntity(ProfesseurDayOffDto dto);
    ProfesseurDayOffDto toDto(ProfesseurDayOff entity);
}
