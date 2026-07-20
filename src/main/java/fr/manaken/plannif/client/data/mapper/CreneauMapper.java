package fr.manaken.plannif.client.data.mapper;

import fr.manaken.plannif.client.data.dto.CreneauDTO;
import fr.manaken.plannif.model.Creneau;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CreneauMapper {

    CreneauDTO toDto(Creneau entity);
    Creneau toEntity(CreneauDTO dto);

    List<CreneauDTO> toDtoList(List<Creneau> entities);
    List<Creneau> toEntityList(List<CreneauDTO> dtos);
}
