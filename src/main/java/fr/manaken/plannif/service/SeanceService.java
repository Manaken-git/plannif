package fr.manaken.plannif.service;

import fr.manaken.plannif.dto.SeanceDTO;
import fr.manaken.plannif.fetcher.DataFetcher;
import fr.manaken.plannif.mapper.SeanceMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SeanceService {
    private final DataFetcher dataFetcher;
    private final SeanceMapper mapper;

    public SeanceService(DataFetcher dataFetcher, SeanceMapper mapper) {
        this.dataFetcher = dataFetcher;
        this.mapper = mapper;
    }

    public List<SeanceDTO> getSeances() {
        return dataFetcher.getSeances().stream().map(mapper::toDto).collect(Collectors.toList());
    }
}
