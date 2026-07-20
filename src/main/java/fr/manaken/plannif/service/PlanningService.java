package fr.manaken.plannif.service;

import fr.manaken.plannif.business.Planning;
import fr.manaken.plannif.client.data.PlannifDataApiClient;
import fr.manaken.plannif.client.data.mapper.ClasseMapper;
import fr.manaken.plannif.client.data.mapper.CreneauMapper;
import fr.manaken.plannif.client.data.mapper.MatiereMapper;
import fr.manaken.plannif.client.data.mapper.ProfesseurMapper;
import fr.manaken.plannif.client.data.mapper.SalleMapper;
import fr.manaken.plannif.client.data.mapper.SeanceMapper;
import fr.manaken.plannif.model.Classe;
import fr.manaken.plannif.model.Creneau;
import fr.manaken.plannif.model.Matiere;
import fr.manaken.plannif.model.Professeur;
import fr.manaken.plannif.model.Salle;
import fr.manaken.plannif.model.Seance;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PlanningService {

    private final PlannifDataApiClient plannifDataApiClient;
    private final ClasseMapper classeMapper;
    private final CreneauMapper creneauMapper;
    private final MatiereMapper matiereMapper;
    private final ProfesseurMapper professeurMapper;
    private final SalleMapper salleMapper;
    private final SeanceMapper seanceMapper;

    public PlanningService(PlannifDataApiClient plannifDataApiClient,
                           ClasseMapper classeMapper,
                           CreneauMapper creneauMapper,
                           MatiereMapper matiereMapper,
                           ProfesseurMapper professeurMapper,
                           SalleMapper salleMapper,
                           SeanceMapper seanceMapper) {
        this.plannifDataApiClient = plannifDataApiClient;
        this.classeMapper = classeMapper;
        this.creneauMapper = creneauMapper;
        this.matiereMapper = matiereMapper;
        this.professeurMapper = professeurMapper;
        this.salleMapper = salleMapper;
        this.seanceMapper = seanceMapper;
    }

    public Planning buildPlanning() {
        Planning planning = new Planning();
        List<Classe> classes = classeMapper.toEntityList(plannifDataApiClient.getClasses());
        List<Professeur> professeurs = professeurMapper.toEntityList(plannifDataApiClient.getProfesseurs());
        List<Matiere> matieres = matiereMapper.toEntityList(plannifDataApiClient.getMatieres());
        List<Salle> salles = salleMapper.toEntityList(plannifDataApiClient.getSalles());
        List<Creneau> creneaux = creneauMapper.toEntityList(plannifDataApiClient.getCreneaux());
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

        planning.setClasses(classes);
        planning.setProfesseurs(professeurs);
        planning.setMatieres(matieres);
        planning.setSalles(salles);
        planning.setCreneaux(creneaux);
        planning.setSeances(seances);

        return planning;
    }
}
