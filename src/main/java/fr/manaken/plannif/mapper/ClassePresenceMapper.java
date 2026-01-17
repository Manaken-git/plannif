package fr.manaken.plannif.mapper;

import fr.manaken.plannif.dto.ClassePresenceDTO;
import fr.manaken.plannif.model.ClassePresence;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = { ClasseMapper.class })
public interface ClassePresenceMapper {
    @org.mapstruct.Mapping(target = "dateDebut", source = "dateDebut")
    @org.mapstruct.Mapping(target = "dateFin", source = "dateFin")
    @org.mapstruct.Mapping(target = "classe", source = "classe")
    public ClassePresenceDTO toDto(ClassePresence entity);
}
