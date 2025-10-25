package fr.manaken.plannif.service;

import fr.manaken.plannif.dto.ProfesseurDTO;
import fr.manaken.plannif.fetcher.DataFetcher;
import fr.manaken.plannif.mapper.ProfesseurMapper;
import fr.manaken.plannif.model.Professeur;
import fr.manaken.plannif.pusher.DataPusher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProfesseurService {

    private final DataFetcher dataFetcher;
    private final DataPusher dataPusher;
    private final ProfesseurMapper mapper;

    public ProfesseurService(DataFetcher dataFetcher, DataPusher dataPusher, ProfesseurMapper mapper) {
        this.dataFetcher = dataFetcher;
        this.dataPusher = dataPusher;
        this.mapper = mapper;
    }

    public List<ProfesseurDTO> getProfesseurs() {
        return dataFetcher.getProfesseurs().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public ProfesseurDTO saveProfesseur(ProfesseurDTO p) {
        Professeur pBDD = null;
        if (p.id() != null) {
            pBDD = dataFetcher.getProfesseur(Math.toIntExact(p.id()));
        }
        mapper.mergeWDTO(pBDD, p);
        return mapper.toDto(dataPusher.saveProfesseur(pBDD));
    }
}
