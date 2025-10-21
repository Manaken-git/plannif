package fr.manaken.plannif.repository;

import fr.manaken.plannif.model.Eleve;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EleveRepository  extends JpaRepository<Eleve, Integer> {

    List<Eleve> findByClasseId(Long idClasse);
}
