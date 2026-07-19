package fr.manaken.plannif.client.data.mapper;

import fr.manaken.plannif.client.data.dto.ProfesseurDto;
import fr.manaken.plannif.model.Professeur;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProfesseurMapper {
    ProfesseurDto toDto(Professeur entity);
    Professeur toEntity(ProfesseurDto dto);
    List<ProfesseurDto> toDtoList(List<Professeur> entities);
    List<Professeur> toEntityList(List<ProfesseurDto> dtos);
}
