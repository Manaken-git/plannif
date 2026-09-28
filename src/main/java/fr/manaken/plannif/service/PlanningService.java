package fr.manaken.plannif.service;

import fr.manaken.plannif.business.Planning;
import fr.manaken.plannif.model.*;
import org.springframework.beans.factory.annotation.Value;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import fr.manaken.plannif.client.data.PlannifDataApiClient;
import fr.manaken.plannif.client.data.mapper.ClasseMapper;
import fr.manaken.plannif.client.data.mapper.MatiereMapper;
import fr.manaken.plannif.client.data.mapper.ProfesseurMapper;
import fr.manaken.plannif.client.data.mapper.SalleMapper;
import fr.manaken.plannif.client.data.mapper.SeanceMapper;
import fr.manaken.plannif.client.data.mapper.MatiereClasseConfigMapper;
import fr.manaken.plannif.client.data.mapper.VacancesMapper;
import fr.manaken.plannif.client.data.dto.PlanningDto;
import fr.manaken.plannif.client.data.dto.CreneauDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
public class PlanningService {

    @Value("${planning.standard-session-duration-hours:2.0}")
    private double standardSessionDurationHours;

    private final PlannifDataApiClient plannifDataApiClient;
    private final ClasseMapper classeMapper;
    private final MatiereMapper matiereMapper;
    private final ProfesseurMapper professeurMapper;
    private final SalleMapper salleMapper;
    private final SeanceMapper seanceMapper;
    private final MatiereClasseConfigMapper matiereClasseConfigMapper;
    private final VacancesMapper vacancesMapper;

    public PlanningService(PlannifDataApiClient plannifDataApiClient,
                           ClasseMapper classeMapper,
                           MatiereMapper matiereMapper,
                           ProfesseurMapper professeurMapper,
                           SalleMapper salleMapper,
                           SeanceMapper seanceMapper,
                           MatiereClasseConfigMapper matiereClasseConfigMapper,
                           VacancesMapper vacancesMapper) {
        this.plannifDataApiClient = plannifDataApiClient;
        this.classeMapper = classeMapper;
        this.matiereMapper = matiereMapper;
        this.professeurMapper = professeurMapper;
        this.salleMapper = salleMapper;
        this.seanceMapper = seanceMapper;
        this.matiereClasseConfigMapper = matiereClasseConfigMapper;
        this.vacancesMapper = vacancesMapper;
    }

    public Planning buildPlanning() {
        Planning planning = new Planning();
        List<Classe> classes = classeMapper.toEntityList(plannifDataApiClient.getClasses());
        List<Professeur> professeurs = professeurMapper.toEntityList(plannifDataApiClient.getProfesseurs());
        List<Matiere> matieres = matiereMapper.toEntityList(plannifDataApiClient.getMatieres());
        List<Salle> salles = salleMapper.toEntityList(plannifDataApiClient.getSalles());
        List<MatiereClasseConfig> matiereClasseConfigs = matiereClasseConfigMapper.toEntityList(plannifDataApiClient.getMatiereClasseConfigs());
        List<Seance> seances = seanceMapper.toEntityList(plannifDataApiClient.getSeances());

        planning.setClasses(classes);
        planning.setProfesseurs(professeurs);
        planning.setMatieres(matieres);
        planning.setSalles(salles);
        planning.setMatiereClasseConfigs(matiereClasseConfigs);
        planning.setSeances(seances);

        List<ClassePresence> allPresences = new ArrayList<>();
        for (Classe c : classes) {
            if (c.getPresences() != null) {
                for (ClassePresence cp : c.getPresences()) {
                    cp.setClasse(c);
                    allPresences.add(cp);
                }
            }
        }
        planning.setClassePresences(allPresences);

        List<ProfesseurDayOff> allDaysOff = new ArrayList<>();
        for (Professeur p : professeurs) {
            if (p.getDaysOff() != null) {
                for (ProfesseurDayOff pdo : p.getDaysOff()) {
                    pdo.setProfesseur(p);
                    allDaysOff.add(pdo);
                }
            }
        }
        planning.setProfesseurDayOffs(allDaysOff);

        List<Vacances> vacances = new ArrayList<>();
        try {
            List<fr.manaken.plannif.client.data.dto.VacancesDto> vacancesDtos = plannifDataApiClient.getVacances();
            if (vacancesDtos != null) {
                vacances = vacancesMapper.toEntityList(vacancesDtos);
            }
        } catch (Exception e) {
            // Log and default to empty
        }
        planning.setVacances(vacances);

        // Fetch candidate dates from plannifDataApiClient creneaux if any exist, otherwise generate
        try {
            List<CreneauDTO> creneauDtos = plannifDataApiClient.getCreneaux();
            if (creneauDtos != null && !creneauDtos.isEmpty()) {
                List<LocalDateTime> dates = creneauDtos.stream()
                        .map(CreneauDTO::debut)
                        .filter(Objects::nonNull)
                        .distinct()
                        .sorted()
                        .collect(Collectors.toList());
                planning.setDatesDebutPossibles(dates);
            }
        } catch (Exception ignored) {
        }

        if (planning.getDatesDebutPossibles() == null || planning.getDatesDebutPossibles().isEmpty()) {
            generateDatesDebutPossiblesIfNeeded(planning);
        }

        linkPresenceContext(planning);

        Map<Long, Classe> classeMap = classes.stream().collect(Collectors.toMap(Classe::getId, c -> c, (a, b) -> a));
        Map<Long, Professeur> profMap = professeurs.stream().collect(Collectors.toMap(Professeur::getId, p -> p, (a, b) -> a));
        Map<Long, Matiere> matiereMap = matieres.stream().collect(Collectors.toMap(Matiere::getId, m -> m, (a, b) -> a));
        Map<Long, Salle> salleMap = salles.stream().collect(Collectors.toMap(Salle::getId, s -> s, (a, b) -> a));

        for (Seance s : seances) {
            if (s.getClasse() != null && s.getClasse().getId() != null) {
                s.setClasse(classeMap.get(s.getClasse().getId()));
            }
            if (s.getProfesseur() != null && s.getProfesseur().getId() != null) {
                s.setProfesseur(profMap.get(s.getProfesseur().getId()));
            }
            if (s.getMatiere() != null && s.getMatiere().getId() != null) {
                s.setMatiere(matiereMap.get(s.getMatiere().getId()));
            }
            if (s.getSalle() != null && s.getSalle().getId() != null) {
                s.setSalle(salleMap.get(s.getSalle().getId()));
            }
        }

        for (MatiereClasseConfig config : matiereClasseConfigs) {
            if (config.getClasse() != null && config.getClasse().getId() != null) {
                config.setClasse(classeMap.get(config.getClasse().getId()));
            }
            if (config.getMatiere() != null && config.getMatiere().getId() != null) {
                config.setMatiere(matiereMap.get(config.getMatiere().getId()));
            }
        }

        return planning;
    }

    public void generateSeancesIfNeeded(Planning planning) {
        linkPresenceContext(planning);
        if (planning.getSeances() == null) {
            planning.setSeances(new ArrayList<>());
        }

        if (planning.getSeances().isEmpty()) {
            List<Seance> generatedSeances = new ArrayList<>();
            long seanceId = 1L;

            Map<Long, Classe> classeMap = planning.getClasses().stream()
                    .collect(Collectors.toMap(Classe::getId, c -> c, (a, b) -> a));
            Map<Long, Matiere> matiereMap = planning.getMatieres().stream()
                    .collect(Collectors.toMap(Matiere::getId, m -> m, (a, b) -> a));

            for (fr.manaken.plannif.model.MatiereClasseConfig config : planning.getMatiereClasseConfigs()) {
                Classe classe = config.getClasse();
                Matiere matiere = config.getMatiere();

                if (classe != null && classe.getId() != null) {
                    classe = classeMap.get(classe.getId());
                }
                if (matiere != null && matiere.getId() != null) {
                    matiere = matiereMap.get(matiere.getId());
                }

                if (classe == null || matiere == null) {
                    continue;
                }

                Long volPeriode = config.getVolumeHorairePeriode();
                if (volPeriode == null || volPeriode <= 0) {
                    continue;
                }

                if (config.getDateDebut() == null || config.getDateFin() == null) {
                    continue;
                }

                int count = (int) Math.round((double) volPeriode / standardSessionDurationHours);

                for (int i = 0; i < count; i++) {
                    Seance seance = new Seance();
                    seance.setId(seanceId++);
                    seance.setClasse(classe);
                    seance.setMatiere(matiere);
                    generatedSeances.add(seance);
                }
            }
            planning.setSeances(generatedSeances);
        }

        generateVieDeClasseSeances(planning);
    }

    private void generateVieDeClasseSeances(Planning planning) {
        if (planning.getClasses() == null || planning.getClassePresences() == null) {
            return;
        }

        List<Seance> existingVdc = planning.getSeances().stream()
                .filter(s -> s.getType() == Seance.TypeSeance.VIE_DE_CLASSE)
                .collect(Collectors.toCollection(ArrayList::new));

        Matiere vdcMatiere = getOrCreateVieDeClasseMatiere(planning);

        for (ClassePresence presence : planning.getClassePresences()) {
            Classe classe = presence.getClasse();
            if (classe == null) {
                continue;
            }

            // 1. Séance Premier jour (09h00 - 10h00)
            if (presence.getFirstDay() != null) {
                ensureVieDeClasseSeance(planning, existingVdc, classe, presence.getFirstDay(), 9, 10, vdcMatiere);
            }

            // 2. Séance Dernier jour (10h00 - 11h00)
            if (presence.getLastDay() != null) {
                ensureVieDeClasseSeance(planning, existingVdc, classe, presence.getLastDay(), 10, 11, vdcMatiere);
            }
        }
    }

    private void ensureVieDeClasseSeance(Planning planning, List<Seance> unassignedPool, Classe classe,
                                         java.time.LocalDate targetDate, int startHour, int endHour, Matiere matiere) {
        LocalDateTime expectedStart = targetDate.atTime(startHour, 0);
        LocalDateTime expectedEnd = targetDate.atTime(endHour, 0);

        boolean alreadyAssigned = planning.getSeances().stream().anyMatch(s ->
                s.getType() == Seance.TypeSeance.VIE_DE_CLASSE
                        && s.getClasse() != null && s.getClasse().getId().equals(classe.getId())
                        && expectedStart.equals(s.getDebut())
                        && expectedEnd.equals(s.getFin()));

        if (!alreadyAssigned) {
            Seance reusable = unassignedPool.stream()
                    .filter(s -> s.getClasse() != null && s.getClasse().getId().equals(classe.getId()) && s.getDebut() == null)
                    .findFirst()
                    .orElse(null);

            if (reusable != null) {
                if (reusable.getMatiere() == null) {
                    reusable.setMatiere(matiere);
                }
                unassignedPool.remove(reusable);
            } else {
                long newId = planning.getSeances().stream().mapToLong(Seance::getId).max().orElse(0L) + 1;
                Seance vdc = new Seance();
                vdc.setId(newId);
                vdc.setClasse(classe);
                vdc.setType(Seance.TypeSeance.VIE_DE_CLASSE);
                vdc.setMatiere(matiere);
                planning.getSeances().add(vdc);
            }
        }
    }

    private Matiere getOrCreateVieDeClasseMatiere(Planning planning) {
        if (planning.getMatieres() == null) {
            planning.setMatieres(new ArrayList<>());
        }
        return planning.getMatieres().stream()
                .filter(m -> m.getNom() != null && m.getNom().equalsIgnoreCase("Vie de classe"))
                .findFirst()
                .orElseGet(() -> {
                    long nextId = planning.getMatieres().stream().mapToLong(Matiere::getId).max().orElse(0L) + 1;
                    Matiere m = new Matiere();
                    m.setId(nextId);
                    m.setNom("Vie de classe");
                    planning.getMatieres().add(m);
                    return m;
                });
    }

    public void linkPresenceContext(Planning planning) {
        if (planning != null && planning.getClassePresences() != null) {
            for (ClassePresence cp : planning.getClassePresences()) {
                if (planning.getVacances() != null) {
                    cp.setVacances(planning.getVacances());
                }
                if (planning.getDatesDebutPossibles() != null) {
                    cp.setAvailableDatesDebut(planning.getDatesDebutPossibles());
                }
            }
        }
    }

    public void generateDatesDebutPossiblesIfNeeded(Planning planning) {
        if (planning.getDatesDebutPossibles() == null || planning.getDatesDebutPossibles().isEmpty()) {
            Set<LocalDateTime> generated = new TreeSet<>();

            if (planning.getClassePresences() != null) {
                for (ClassePresence presence : planning.getClassePresences()) {
                    LocalDate start = presence.getDateDebut();
                    LocalDate end = presence.getDateFin();
                    if (start == null || end == null) {
                        continue;
                    }
                    for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                        if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                            continue;
                        }

                        // 1h slots start times:
                        // 8h (except Monday)
                        if (date.getDayOfWeek() != DayOfWeek.MONDAY) {
                            generated.add(date.atTime(8, 0));
                        }
                        // 9h, 10h, 11h
                        generated.add(date.atTime(9, 0));
                        generated.add(date.atTime(10, 0));
                        generated.add(date.atTime(11, 0));
                        // Afternoon slots (except Friday)
                        if (date.getDayOfWeek() != DayOfWeek.FRIDAY) {
                            generated.add(date.atTime(13, 0));
                            generated.add(date.atTime(14, 0));
                            generated.add(date.atTime(15, 0));
                            generated.add(date.atTime(16, 0));
                        }

                        // 1h30 TP slots start times:
                        // 8h (except Monday) - already added above
                        generated.add(date.atTime(9, 30));
                        // 11h - already added above
                        if (date.getDayOfWeek() != DayOfWeek.FRIDAY) {
                            // 13h - already added above
                            generated.add(date.atTime(14, 30));
                            // 16h - already added above
                        }
                    }
                }
            }
            planning.setDatesDebutPossibles(new ArrayList<>(generated));
        }
    }

    public void generateCreneauxIfNeeded(Planning planning) {
        generateDatesDebutPossiblesIfNeeded(planning);
    }

    public void savePlanning(Planning planning) {
        if (planning.getNom() == null) {
            planning.setNom("Planning généré le " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        }
        if (planning.getDateCreation() == null) {
            planning.setDateCreation(java.time.LocalDateTime.now());
        }

        if (planning.getSeances() != null) {
            for (Seance s : planning.getSeances()) {
                if (s.getDebut() != null) {
                    s.setFin(s.getFin());
                }
            }
        }

        var seanceDtos = seanceMapper.toDtoList(planning.getSeances());
        PlanningDto planningDto = PlanningDto.builder()
                .id(planning.getId())
                .nom(planning.getNom())
                .dateCreation(planning.getDateCreation())
                .seances(seanceDtos)
                .creneaux(List.of())
                .build();

        PlanningDto saved = plannifDataApiClient.savePlanning(planningDto);
        planning.setId(saved.getId());
    }
}
