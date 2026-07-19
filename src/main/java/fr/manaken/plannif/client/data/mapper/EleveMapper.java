package fr.manaken.plannif.client.data.mapper;

import fr.manaken.plannif.client.data.dto.EleveDto;
import fr.manaken.plannif.model.Eleve;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EleveMapper {
    @Mapping(source = "classe.id", target = "classeId")
    EleveDto toDto(Eleve entity);

    @Mapping(source = "classeId", target = "classe.id")
    Eleve toEntity(EleveDto dto);

    List<EleveDto> toDtoList(List<Eleve> entities);
    List<Eleve> toEntityList(List<EleveDto> dtos);
}
