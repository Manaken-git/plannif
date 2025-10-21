package fr.manaken.plannif.service;

import fr.manaken.plannif.dto.EleveDTO;
import fr.manaken.plannif.fetcher.DataFetcher;
import fr.manaken.plannif.mapper.EleveMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EleveService {

    private final DataFetcher dataFetcher;
    private final EleveMapper mapper;

    public EleveService(DataFetcher dataFetcher, EleveMapper mapper) {
        this.dataFetcher = dataFetcher;
        this.mapper = mapper;
    }
    public List<EleveDTO> getEleves(Long idClasse) {
        return dataFetcher.getElevesByClasse(idClasse).stream().map(mapper::toDto).collect(Collectors.toList());
    }
}
