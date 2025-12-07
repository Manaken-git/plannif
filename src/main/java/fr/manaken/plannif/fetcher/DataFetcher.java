package fr.manaken.plannif.fetcher;

import fr.manaken.plannif.model.Eleve;
import fr.manaken.plannif.model.Professeur;
import fr.manaken.plannif.model.Seance;
import fr.manaken.plannif.repository.EleveRepository;
import fr.manaken.plannif.repository.ProfesseurRepository;
import fr.manaken.plannif.repository.SeanceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataFetcher {

    private final ProfesseurRepository professeurRepository;
    private final SeanceRepository seanceRepository;
    private final EleveRepository eleveRepository;

    public DataFetcher(ProfesseurRepository professeurRepository, SeanceRepository seanceRepository,
            EleveRepository eleveRepository) {
        this.professeurRepository = professeurRepository;
        this.seanceRepository = seanceRepository;
        this.eleveRepository = eleveRepository;
    }

    public List<Professeur> getProfesseurs() {
        return professeurRepository.findAll();
    }

    public List<Seance> getSeances() {
        return seanceRepository.findAll();
    }

    public List<Eleve> getElevesByClasse(Long idClasse) {
        return eleveRepository.findByClasseId(idClasse);
    }

    @SuppressWarnings("null")
    public Professeur getProfesseur(Integer id) {
        return professeurRepository.getReferenceById(id);
    }
}
