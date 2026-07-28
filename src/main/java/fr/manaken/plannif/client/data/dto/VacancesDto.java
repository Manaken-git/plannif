package fr.manaken.plannif.client.data.dto;

import java.time.LocalDate;

public record VacancesDto(
        Long id,
        String nom,
        LocalDate dateDebut,
        LocalDate dateFin
) {}
