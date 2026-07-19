package fr.manaken.plannif.client.data.mapper;

import fr.manaken.plannif.client.data.dto.MatiereDto;
import fr.manaken.plannif.model.Matiere;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MatiereMapper {
    MatiereDto toDto(Matiere entity);
    Matiere toEntity(MatiereDto dto);
    List<MatiereDto> toDtoList(List<Matiere> entities);
    List<Matiere> toEntityList(List<MatiereDto> dtos);
}
