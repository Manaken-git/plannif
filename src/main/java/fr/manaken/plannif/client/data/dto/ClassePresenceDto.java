package fr.manaken.plannif.client.data.dto;

import java.time.LocalDate;

public record ClassePresenceDto(
        Long id,
        LocalDate dateDebut,
        LocalDate dateFin
) {}
