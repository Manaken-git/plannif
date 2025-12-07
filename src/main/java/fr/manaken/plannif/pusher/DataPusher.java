package fr.manaken.plannif.pusher;

import fr.manaken.plannif.model.Professeur;
import fr.manaken.plannif.repository.ProfesseurRepository;
import org.springframework.stereotype.Service;

@Service
public class DataPusher {
    private final ProfesseurRepository professeurRepository;

    public DataPusher(ProfesseurRepository professeurRepository) {
        this.professeurRepository = professeurRepository;
    }

    @SuppressWarnings("null")
    public Professeur saveProfesseur(Professeur p) {
        return professeurRepository.save(p);
    }

}
