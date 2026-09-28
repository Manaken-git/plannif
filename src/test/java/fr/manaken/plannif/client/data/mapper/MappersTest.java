package fr.manaken.plannif.client.data.mapper;

import fr.manaken.plannif.client.data.dto.*;
import fr.manaken.plannif.model.*;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MappersTest {

    private final MatiereMapper matiereMapper = Mappers.getMapper(MatiereMapper.class);
    private final SalleMapper salleMapper = Mappers.getMapper(SalleMapper.class);
    private final ClasseMapper classeMapper = Mappers.getMapper(ClasseMapper.class);
    private final EleveMapper eleveMapper = Mappers.getMapper(EleveMapper.class);
    private final ProfesseurMapper professeurMapper = Mappers.getMapper(ProfesseurMapper.class);
    private final SeanceMapper seanceMapper = Mappers.getMapper(SeanceMapper.class);

    @Test
    void testMatiereMapping() {
        Matiere entity = new Matiere();
        entity.setId(1L);
        entity.setNom("Maths");

        MatiereDto dto = matiereMapper.toDto(entity);
        assertEquals(1L, dto.getId());
        assertEquals("Maths", dto.getNom());

        Matiere mappedEntity = matiereMapper.toEntity(dto);
        assertEquals(entity.getId(), mappedEntity.getId());
        assertEquals(entity.getNom(), mappedEntity.getNom());
    }

    @Test
    void testSalleMapping() {
        Salle entity = new Salle();
        entity.setId(2L);
        entity.setCode("B203");
        entity.setCapacite(30);
        entity.setType("TP");

        SalleDto dto = salleMapper.toDto(entity);
        assertEquals(2L, dto.getId());
        assertEquals("B203", dto.getCode());

        Salle mapped = salleMapper.toEntity(dto);
        assertEquals("B203", mapped.getCode());
        assertEquals(30, mapped.getCapacite());
    }

    @Test
    void testClasseMapping() {
        Classe entity = new Classe();
        entity.setId(3L);
        entity.setNom("6eme A");

        ClasseDto dto = classeMapper.toDto(entity);
        assertEquals(3L, dto.getId());
        assertEquals("6eme A", dto.getNom());

        Classe mapped = classeMapper.toEntity(dto);
        assertEquals("6eme A", mapped.getNom());
    }

    @Test
    void testEleveMapping() {
        Classe classe = new Classe();
        classe.setId(3L);

        Eleve entity = new Eleve();
        entity.setId(4L);
        entity.setNom("Dupont");
        entity.setPrenom("Jean");
        entity.setClasse(classe);

        EleveDto dto = eleveMapper.toDto(entity);
        assertEquals(4L, dto.getId());
        assertEquals("Dupont", dto.getNom());
        assertEquals(3L, dto.getClasseId());

        Eleve mapped = eleveMapper.toEntity(dto);
        assertEquals(4L, mapped.getId());
        assertNotNull(mapped.getClasse());
        assertEquals(3L, mapped.getClasse().getId());
    }

    @Test
    void testProfesseurMapping() {
        Professeur entity = new Professeur();
        entity.setId(5L);
        entity.setNom("Curie");
        entity.setPrenom("Marie");
        entity.setEmail("marie.curie@test.fr");
        entity.setNb_heures(BigDecimal.valueOf(35));

        ProfesseurDto dto = professeurMapper.toDto(entity);
        assertEquals(5L, dto.getId());
        assertEquals("Curie", dto.getNom());
        assertEquals(BigDecimal.valueOf(35), dto.getNb_heures());

        Professeur mapped = professeurMapper.toEntity(dto);
        assertEquals("Curie", mapped.getNom());
    }

    @Test
    void testSeanceMapping() {
        Professeur prof = new Professeur();
        prof.setId(10L);

        Classe classe = new Classe();
        classe.setId(20L);

        Matiere matiere = new Matiere();
        matiere.setId(30L);

        Salle salle = new Salle();
        salle.setId(40L);

        java.time.LocalDateTime debut = java.time.LocalDateTime.of(2024, 2, 12, 8, 0);
        java.time.LocalDateTime fin = java.time.LocalDateTime.of(2024, 2, 12, 9, 0);

        Seance entity = new Seance();
        entity.setId(1L);
        entity.setProfesseur(prof);
        entity.setClasse(classe);
        entity.setMatiere(matiere);
        entity.setSalle(salle);
        entity.setDebut(debut);
        entity.setType(Seance.TypeSeance.COURS);

        SeanceDto dto = seanceMapper.toDto(entity);
        assertEquals(1L, dto.getId());
        assertEquals(10L, dto.getProfesseurId());
        assertEquals(20L, dto.getClasseId());
        assertEquals(30L, dto.getMatiereId());
        assertEquals(40L, dto.getSalleId());
        assertEquals(debut, dto.getDebut());
        assertEquals(fin, dto.getFin());
        assertEquals("COURS", dto.getType());

        Seance mapped = seanceMapper.toEntity(dto);
        assertEquals(1L, mapped.getId());
        assertEquals(10L, mapped.getProfesseur().getId());
        assertEquals(20L, mapped.getClasse().getId());
        assertEquals(30L, mapped.getMatiere().getId());
        assertEquals(40L, mapped.getSalle().getId());
        assertEquals(debut, mapped.getDebut());
        assertEquals(fin, mapped.getFin());
        assertEquals(Seance.TypeSeance.COURS, mapped.getType());
    }
}
