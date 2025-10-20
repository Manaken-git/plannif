package fr.manaken.plannif.repository;

import fr.manaken.plannif.model.Classe;
import fr.manaken.plannif.model.Professeur;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClasseRepository extends JpaRepository<Classe, Integer> {
}
