package fr.manaken.plannif.dto;

import java.math.BigDecimal;

public record ProfesseurDTO(Long id, String nom, String prenom, String email, BigDecimal nb_heures) {}
