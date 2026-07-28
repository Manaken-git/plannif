package fr.manaken.plannif.client.data.mapper;

import fr.manaken.plannif.client.data.dto.VacancesDto;
import fr.manaken.plannif.model.Vacances;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VacancesMapper {
    VacancesDto toDto(Vacances entity);
    Vacances toEntity(VacancesDto dto);
    List<VacancesDto> toDtoList(List<Vacances> entities);
    List<Vacances> toEntityList(List<VacancesDto> dtos);
}
