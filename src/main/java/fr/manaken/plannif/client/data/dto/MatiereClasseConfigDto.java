package fr.manaken.plannif.client.data.dto;

import java.time.LocalDate;

public record MatiereClasseConfigDto(
        Long id,
        Long classeId,
        String classeNom,
        Long matiereId,
        String matiereNom,
        LocalDate dateDebut,
        LocalDate dateFin
) {}
