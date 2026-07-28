package fr.manaken.plannif.client.data.mapper;

import fr.manaken.plannif.client.data.dto.ClassePresenceDto;
import fr.manaken.plannif.model.ClassePresence;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClassePresenceMapper {
    ClassePresenceDto toDto(ClassePresence entity);
    ClassePresence toEntity(ClassePresenceDto dto);
}
