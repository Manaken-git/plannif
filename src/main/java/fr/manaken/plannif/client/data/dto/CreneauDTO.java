package fr.manaken.plannif.client.data.dto;

import fr.manaken.plannif.model.SemaineType;
import java.time.LocalDateTime;

public record CreneauDTO(Long id, LocalDateTime debut, LocalDateTime fin, SemaineType semaineType) {}

