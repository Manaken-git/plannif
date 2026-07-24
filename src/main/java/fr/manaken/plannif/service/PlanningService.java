package fr.manaken.plannif.service;

import fr.manaken.plannif.business.Planning;
import fr.manaken.plannif.model.*;
import org.springframework.beans.factory.annotation.Value;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import fr.manaken.plannif.client.data.PlannifDataApiClient;
import fr.manaken.plannif.client.data.mapper.ClasseMapper;
import fr.manaken.plannif.client.data.mapper.CreneauMapper;
import fr.manaken.plannif.client.data.mapper.MatiereMapper;
import fr.manaken.plannif.client.data.mapper.ProfesseurMapper;
import fr.manaken.plannif.client.data.mapper.SalleMapper;
import fr.manaken.plannif.client.data.mapper.SeanceMapper;
import fr.manaken.plannif.client.data.mapper.MatiereClasseConfigMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PlanningService {

    @Value("${planning.teaching-weeks:36}")
    private int teachingWeeks;

    @Value("${planning.standard-session-duration-hours:2.0}")
    private double standardSessionDurationHours;

    private final PlannifDataApiClient plannifDataApiClient;
    private final ClasseMapper classeMapper;
    private final CreneauMapper creneauMapper;
    private final MatiereMapper matiereMapper;
    private final ProfesseurMapper professeurMapper;
    private final SalleMapper salleMapper;
    private final SeanceMapper seanceMapper;
    private final MatiereClasseConfigMapper matiereClasseConfigMapper;

    public PlanningService(PlannifDataApiClient plannifDataApiClient,
                           ClasseMapper classeMapper,
                           CreneauMapper creneauMapper,
                           MatiereMapper matiereMapper,
                           ProfesseurMapper professeurMapper,
                           SalleMapper salleMapper,
                           SeanceMapper seanceMapper,
                           MatiereClasseConfigMapper matiereClasseConfigMapper) {
        this.plannifDataApiClient = plannifDataApiClient;
        this.classeMapper = classeMapper;
        this.creneauMapper = creneauMapper;
        this.matiereMapper = matiereMapper;
        this.professeurMapper = professeurMapper;
        this.salleMapper = salleMapper;
        this.seanceMapper = seanceMapper;
        this.matiereClasseConfigMapper = matiereClasseConfigMapper;
    }

    public Planning buildPlanning() {
        Planning planning = new Planning();
        List<Classe> classes = classeMapper.toEntityList(plannifDataApiClient.getClasses());
        List<Professeur> professeurs = professeurMapper.toEntityList(plannifDataApiClient.getProfesseurs());
        List<Matiere> matieres = matiereMapper.toEntityList(plannifDataApiClient.getMatieres());
        List<Salle> salles = salleMapper.toEntityList(plannifDataApiClient.getSalles());
        List<Creneau> creneaux = creneauMapper.toEntityList(plannifDataApiClient.getCreneaux());
        List<MatiereClasseConfig> matiereClasseConfigs = matiereClasseConfigMapper.toEntityList(plannifDataApiClient.getMatiereClasseConfigs());
        List<Seance> seances = seanceMapper.toEntityList(plannifDataApiClient.getSeances());

        Map<Long, Classe> classeMap = classes.stream().collect(Collectors.toMap(Classe::getId, c -> c, (a, b) -> a));
        Map<Long, Professeur> profMap = professeurs.stream().collect(Collectors.toMap(Professeur::getId, p -> p, (a, b) -> a));
        Map<Long, Matiere> matiereMap = matieres.stream().collect(Collectors.toMap(Matiere::getId, m -> m, (a, b) -> a));
        Map<Long, Salle> salleMap = salles.stream().collect(Collectors.toMap(Salle::getId, s -> s, (a, b) -> a));
        Map<Long, Creneau> creneauMap = creneaux.stream().collect(Collectors.toMap(Creneau::getId, c -> c, (a, b) -> a));

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
            if (s.getCreneau() != null && s.getCreneau().getId() != null) {
                s.setCreneau(creneauMap.get(s.getCreneau().getId()));
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

        planning.setClasses(classes);
        planning.setProfesseurs(professeurs);
        planning.setMatieres(matieres);
        planning.setSalles(salles);
        planning.setCreneaux(creneaux);
        planning.setMatiereClasseConfigs(matiereClasseConfigs);
        planning.setSeances(seances);

        return planning;
    }

    public void generateSeancesIfNeeded(Planning planning) {
        if (planning.getSeances() != null && !planning.getSeances().isEmpty()) {
            return;
        }

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

            Long volAnnuel = matiere.getVolumeHoraireAnnuel();
            if (volAnnuel == null || volAnnuel <= 0) {
                continue;
            }

            if (config.getDateDebut() == null || config.getDateFin() == null) {
                continue;
            }

            double volHebdo = (double) volAnnuel / (double) teachingWeeks;
            long days = ChronoUnit.DAYS.between(config.getDateDebut(), config.getDateFin()) + 1;
            double weeks = (double) days / 7.0;
            double totalHours = volHebdo * weeks;
            int count = (int) Math.round(totalHours / standardSessionDurationHours);

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
}
