package fr.manaken.plannif.fetcher;

import fr.manaken.plannif.model.Professeur;
import fr.manaken.plannif.repository.ProfesseurRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataFetcher {


    private final ProfesseurRepository professeurRepository;

    public DataFetcher(ProfesseurRepository professeurRepository) {
        this.professeurRepository = professeurRepository;
    }

    public List<Professeur> getProfesseurs() {
        return professeurRepository.findAll();
    }


}
