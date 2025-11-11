package fr.manaken.plannif.dto;

import java.time.LocalDateTime;

public record SeanceDTO(
        Long id,
        LocalDateTime debut,
        LocalDateTime fin,
        String professeurNomComplet,
        String classeNom,
        String matiereNom,
        String salleCode
) {}
