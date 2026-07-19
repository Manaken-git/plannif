package fr.manaken.plannif.client.data.mapper;

import fr.manaken.plannif.client.data.dto.ClasseDto;
import fr.manaken.plannif.model.Classe;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClasseMapper {
    ClasseDto toDto(Classe entity);
    Classe toEntity(ClasseDto dto);
    List<ClasseDto> toDtoList(List<Classe> entities);
    List<Classe> toEntityList(List<ClasseDto> dtos);
}
