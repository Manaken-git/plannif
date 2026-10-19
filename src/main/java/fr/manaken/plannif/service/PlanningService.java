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
import fr.manaken.plannif.client.data.mapper.EleveMapper;
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
    private final EleveMapper eleveMapper;

    public PlanningService(PlannifDataApiClient plannifDataApiClient,
                           ClasseMapper classeMapper,
                           MatiereMapper matiereMapper,
                           ProfesseurMapper professeurMapper,
                           SalleMapper salleMapper,
                           SeanceMapper seanceMapper,
                           MatiereClasseConfigMapper matiereClasseConfigMapper,
                           VacancesMapper vacancesMapper,
                           EleveMapper eleveMapper) {
        this.plannifDataApiClient = plannifDataApiClient;
        this.classeMapper = classeMapper;
        this.matiereMapper = matiereMapper;
        this.professeurMapper = professeurMapper;
        this.salleMapper = salleMapper;
        this.seanceMapper = seanceMapper;
        this.matiereClasseConfigMapper = matiereClasseConfigMapper;
        this.vacancesMapper = vacancesMapper;
        this.eleveMapper = eleveMapper;
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
        List<ProfesseurIndisponibilite> allIndispos = new ArrayList<>();
        for (Professeur p : professeurs) {
            if (p.getDaysOff() != null) {
                for (ProfesseurDayOff pdo : p.getDaysOff()) {
                    pdo.setProfesseur(p);
                    allDaysOff.add(pdo);
                }
            }
            if (p.getIndisponibilites() != null) {
                for (ProfesseurIndisponibilite pi : p.getIndisponibilites()) {
                    pi.setProfesseur(p);
                    allIndispos.add(pi);
                }
            }
        }
        planning.setProfesseurDayOffs(allDaysOff);
        planning.setProfesseurIndisponibilites(allIndispos);

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

        List<Eleve> eleves = new ArrayList<>();
        try {
            List<fr.manaken.plannif.client.data.dto.EleveDto> eleveDtos = plannifDataApiClient.getEleves();
            if (eleveDtos != null && !eleveDtos.isEmpty()) {
                eleves = eleveMapper.toEntityList(eleveDtos);
            }
        } catch (Exception ignored) {
        }

        Map<Long, Classe> classeMap = classes.stream().collect(Collectors.toMap(Classe::getId, c -> c, (a, b) -> a));
        for (Eleve el : eleves) {
            if (el.getClasse() != null && el.getClasse().getId() != null) {
                Classe c = classeMap.get(el.getClasse().getId());
                if (c != null) {
                    el.setClasse(c);
                    c.getEleves().add(el);
                }
            }
        }
        Map<Long, Professeur> profMap = professeurs.stream().collect(Collectors.toMap(Professeur::getId, p -> p, (a, b) -> a));
        Map<Long, Matiere> matiereMap = matieres.stream().collect(Collectors.toMap(Matiere::getId, m -> m, (a, b) -> a));
        Map<Long, Salle> salleMap = salles.stream().collect(Collectors.toMap(Salle::getId, s -> s, (a, b) -> a));

        for (Classe c : classes) {
            if (c.getReferents() != null) {
                for (ClasseReferent cr : c.getReferents()) {
                    cr.setClasse(c);
                    if (cr.getProfesseur() != null && cr.getProfesseur().getId() != null) {
                        Professeur resolved = profMap.get(cr.getProfesseur().getId());
                        if (resolved != null) {
                            cr.setProfesseur(resolved);
                        }
                    }
                }
            }
        }

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
                    seance.setType(Seance.TypeSeance.COURS);
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
        int durationMinutes = (int) java.time.Duration.between(expectedStart, expectedEnd).toMinutes();

        Seance existingAssigned = planning.getSeances().stream().filter(s ->
                s.getType() == Seance.TypeSeance.VIE_DE_CLASSE
                        && s.getClasse() != null && s.getClasse().getId().equals(classe.getId())
                        && expectedStart.equals(s.getDebut())
                        && expectedEnd.equals(s.getFin()))
                .findFirst()
                .orElse(null);

        if (existingAssigned != null) {
            existingAssigned.setPinned(true);
            existingAssigned.setDureeMinutes(durationMinutes);
            if (existingAssigned.getProfesseur() == null) {
                existingAssigned.setProfesseur(selectProfesseurForVieDeClasse(planning, classe, expectedStart, expectedEnd));
            }
            if (existingAssigned.getSalle() == null) {
                existingAssigned.setSalle(findAvailableSalleForVieDeClasse(planning, classe, expectedStart, expectedEnd));
            }
            return;
        }

        Professeur chosenProf = selectProfesseurForVieDeClasse(planning, classe, expectedStart, expectedEnd);
        Salle chosenSalle = findAvailableSalleForVieDeClasse(planning, classe, expectedStart, expectedEnd);

        Seance reusable = unassignedPool.stream()
                .filter(s -> s.getClasse() != null && s.getClasse().getId().equals(classe.getId()) && s.getDebut() == null)
                .findFirst()
                .orElse(null);

        if (reusable != null) {
            if (reusable.getMatiere() == null) {
                reusable.setMatiere(matiere);
            }
            reusable.setDebut(expectedStart);
            reusable.setFin(expectedEnd);
            reusable.setDureeMinutes(durationMinutes);
            reusable.setPinned(true);
            if (reusable.getProfesseur() == null) {
                reusable.setProfesseur(chosenProf);
            }
            if (reusable.getSalle() == null) {
                reusable.setSalle(chosenSalle);
            }
            unassignedPool.remove(reusable);
        } else {
            long newId = planning.getSeances().stream().mapToLong(Seance::getId).max().orElse(0L) + 1;
            Seance vdc = new Seance();
            vdc.setId(newId);
            vdc.setClasse(classe);
            vdc.setType(Seance.TypeSeance.VIE_DE_CLASSE);
            vdc.setMatiere(matiere);
            vdc.setDebut(expectedStart);
            vdc.setFin(expectedEnd);
            vdc.setDureeMinutes(durationMinutes);
            vdc.setPinned(true);
            vdc.setProfesseur(chosenProf);
            vdc.setSalle(chosenSalle);
            planning.getSeances().add(vdc);
        }
    }

    private Professeur selectProfesseurForVieDeClasse(Planning planning, Classe classe, LocalDateTime start, LocalDateTime end) {
        if (classe == null) {
            return null;
        }

        Professeur principal = classe.getProfesseurReferentPrincipal();
        if (principal != null && isProfesseurAvailable(planning, principal, start, end)) {
            return principal;
        }

        // Fallback to co-referents if principal is unavailable
        if (classe.getReferents() != null) {
            for (ClasseReferent ref : classe.getReferents()) {
                if (ref.getProfesseur() != null && isProfesseurAvailable(planning, ref.getProfesseur(), start, end)) {
                    return ref.getProfesseur();
                }
            }
        }

        // Fallback to principal or first declared referent if all have constraints
        if (principal != null) {
            return principal;
        }
        if (classe.getReferents() != null && !classe.getReferents().isEmpty()) {
            return classe.getReferents().get(0).getProfesseur();
        }
        return null;
    }

    private boolean isProfesseurAvailable(Planning planning, Professeur prof, LocalDateTime debut, LocalDateTime fin) {
        if (prof == null || debut == null || fin == null) {
            return false;
        }

        // Check Day Off (0 = Monday .. 4 = Friday)
        int dow = debut.getDayOfWeek().getValue() - 1;
        if (planning.getProfesseurDayOffs() != null) {
            boolean hasDayOff = planning.getProfesseurDayOffs().stream()
                    .anyMatch(d -> d.getProfesseur() != null && d.getProfesseur().getId().equals(prof.getId())
                            && d.getDayOfWeek() != null && d.getDayOfWeek() == dow);
            if (hasDayOff) {
                return false;
            }
        }

        // Check indisponibilites
        if (planning.getProfesseurIndisponibilites() != null) {
            boolean hasIndisp = planning.getProfesseurIndisponibilites().stream()
                    .anyMatch(indisp -> indisp.getProfesseur() != null && indisp.getProfesseur().getId().equals(prof.getId())
                            && indisp.matches(debut, fin));
            if (hasIndisp) {
                return false;
            }
        }

        // Check if teacher is already assigned to another pinned session at this slot
        if (planning.getSeances() != null) {
            boolean busy = planning.getSeances().stream()
                    .anyMatch(s -> s.isPinned() && s.getProfesseur() != null && s.getProfesseur().getId().equals(prof.getId())
                            && s.getDebut() != null && s.getFin() != null
                            && s.getDebut().isBefore(fin) && s.getFin().isAfter(debut));
            if (busy) {
                return false;
            }
        }

        return true;
    }

    private Salle findAvailableSalleForVieDeClasse(Planning planning, Classe classe, LocalDateTime start, LocalDateTime end) {
        if (planning.getSalles() == null || planning.getSalles().isEmpty()) {
            return null;
        }

        int classSize = (classe != null && classe.getEleves() != null) ? classe.getEleves().size() : 0;

        List<Salle> candidates = planning.getSalles().stream()
                .filter(salle -> {
                    // Check capacity
                    if (classSize > 0 && salle.getCapacite() != null && salle.getCapacite() < classSize) {
                        return false;
                    }
                    // Check if already assigned to another pinned session at this slot
                    if (planning.getSeances() != null) {
                        boolean roomBusy = planning.getSeances().stream().anyMatch(s ->
                                s.isPinned() && s.getSalle() != null && s.getSalle().getId().equals(salle.getId())
                                        && s.getDebut() != null && s.getFin() != null
                                        && s.getDebut().isBefore(end) && s.getFin().isAfter(start));
                        if (roomBusy) {
                            return false;
                        }
                    }
                    return true;
                })
                .sorted((s1, s2) -> {
                    boolean s1Cours = s1.getType() == null || "COURS".equalsIgnoreCase(s1.getType()) || "STANDARD".equalsIgnoreCase(s1.getType());
                    boolean s2Cours = s2.getType() == null || "COURS".equalsIgnoreCase(s2.getType()) || "STANDARD".equalsIgnoreCase(s2.getType());
                    if (s1Cours && !s2Cours) return -1;
                    if (!s1Cours && s2Cours) return 1;

                    int cap1 = s1.getCapacite() != null ? s1.getCapacite() : 999;
                    int cap2 = s2.getCapacite() != null ? s2.getCapacite() : 999;
                    return Integer.compare(cap1, cap2);
                })
                .collect(Collectors.toList());

        return candidates.isEmpty() ? planning.getSalles().get(0) : candidates.get(0);
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
