package fr.manaken.plannif.service;

import fr.manaken.plannif.fetcher.DataFetcher;
import fr.manaken.plannif.model.Professeur;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfesseurService {

    private final DataFetcher dataFetcher;

    public ProfesseurService(DataFetcher dataFetcher) {
        this.dataFetcher = dataFetcher;
    }

    public List<Professeur> getProfesseurs() {
        return dataFetcher.getProfesseurs();
    }
}
