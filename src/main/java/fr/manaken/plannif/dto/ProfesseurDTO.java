package fr.manaken.plannif.dto;

import java.math.BigDecimal;
import java.util.Set;

public record ProfesseurDTO(
    Long id,
    String nom,
    String prenom,
    String email,
    BigDecimal nb_heures,
    PlageHoraireDTO plageHorairePreferee,
    Set<SeanceDTO> seances) {}
