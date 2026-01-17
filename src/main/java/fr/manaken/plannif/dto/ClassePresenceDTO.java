package fr.manaken.plannif.dto;

import java.time.LocalDate;

public record ClassePresenceDTO(
        Long id,
        ClasseDTO classe,
        LocalDate dateDebut,
        LocalDate dateFin) {
}
