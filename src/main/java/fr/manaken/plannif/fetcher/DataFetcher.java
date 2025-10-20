package fr.manaken.plannif.fetcher;

import fr.manaken.plannif.model.Professeur;
import fr.manaken.plannif.model.Seance;
import fr.manaken.plannif.repository.ProfesseurRepository;
import fr.manaken.plannif.repository.SeanceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataFetcher {


    private final ProfesseurRepository professeurRepository;
    private final SeanceRepository seanceRepository;

    public DataFetcher(ProfesseurRepository professeurRepository, SeanceRepository seanceRepository) {
        this.professeurRepository = professeurRepository;
        this.seanceRepository = seanceRepository;
    }

    public List<Professeur> getProfesseurs() {
        return professeurRepository.findAll();
    }

    public List<Seance> getSeances() {
        return seanceRepository.findAll();
    }


}
