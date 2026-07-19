package fr.manaken.plannif.client.data.mapper;

import fr.manaken.plannif.client.data.dto.SeanceDto;
import fr.manaken.plannif.model.Seance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SeanceMapper {
    @Mapping(source = "professeur.id", target = "professeurId")
    @Mapping(source = "classe.id", target = "classeId")
    @Mapping(source = "matiere.id", target = "matiereId")
    @Mapping(source = "salle.id", target = "salleId")
    @Mapping(source = "creneau.id", target = "creneauId")
    SeanceDto toDto(Seance entity);

    @Mapping(source = "professeurId", target = "professeur.id")
    @Mapping(source = "classeId", target = "classe.id")
    @Mapping(source = "matiereId", target = "matiere.id")
    @Mapping(source = "salleId", target = "salle.id")
    @Mapping(source = "creneauId", target = "creneau.id")
    Seance toEntity(SeanceDto dto);

    List<SeanceDto> toDtoList(List<Seance> entities);
    List<Seance> toEntityList(List<SeanceDto> dtos);
}
