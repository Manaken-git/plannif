package fr.manaken.plannif.client.data.mapper;

import fr.manaken.plannif.client.data.dto.MatiereClasseConfigDto;
import fr.manaken.plannif.model.MatiereClasseConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MatiereClasseConfigMapper {

    @Mapping(source = "classe.id", target = "classeId")
    @Mapping(source = "classe.nom", target = "classeNom")
    @Mapping(source = "matiere.id", target = "matiereId")
    @Mapping(source = "matiere.nom", target = "matiereNom")
    MatiereClasseConfigDto toDto(MatiereClasseConfig entity);

    @Mapping(source = "classeId", target = "classe.id")
    @Mapping(source = "matiereId", target = "matiere.id")
    MatiereClasseConfig toEntity(MatiereClasseConfigDto dto);

    List<MatiereClasseConfigDto> toDtoList(List<MatiereClasseConfig> entities);
    List<MatiereClasseConfig> toEntityList(List<MatiereClasseConfigDto> dtos);
}
