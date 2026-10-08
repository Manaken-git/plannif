package fr.manaken.plannif.client.data.dto;

import java.time.LocalDate;

public record MatiereClasseConfigDto(
        Long id,
        Long classeId,
        String classeNom,
        Long matiereId,
        String matiereNom,
        LocalDate dateDebut,
        LocalDate dateFin,
        Long volumeHorairePeriode,
        String typeSalleRequis,
        Boolean prioriteMatin
) {
    public MatiereClasseConfigDto(
            Long id,
            Long classeId,
            String classeNom,
            Long matiereId,
            String matiereNom,
            LocalDate dateDebut,
            LocalDate dateFin,
            Long volumeHorairePeriode) {
        this(id, classeId, classeNom, matiereId, matiereNom, dateDebut, dateFin, volumeHorairePeriode, null, null);
    }
}
