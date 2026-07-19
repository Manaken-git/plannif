package fr.manaken.plannif.service;

import fr.manaken.plannif.business.Planning;
import fr.manaken.plannif.client.data.PlannifDataApiClient;
import fr.manaken.plannif.client.data.mapper.ClasseMapper;
import fr.manaken.plannif.client.data.mapper.MatiereMapper;
import fr.manaken.plannif.client.data.mapper.ProfesseurMapper;
import fr.manaken.plannif.client.data.mapper.SalleMapper;
import fr.manaken.plannif.client.data.mapper.SeanceMapper;
import org.springframework.stereotype.Service;

@Service
public class PlanningService {

    private final PlannifDataApiClient plannifDataApiClient;
    private final ClasseMapper classeMapper;
    private final MatiereMapper matiereMapper;
    private final ProfesseurMapper professeurMapper;
    private final SalleMapper salleMapper;
    private final SeanceMapper seanceMapper;

    public PlanningService(PlannifDataApiClient plannifDataApiClient,
                           ClasseMapper classeMapper,
                           MatiereMapper matiereMapper,
                           ProfesseurMapper professeurMapper,
                           SalleMapper salleMapper,
                           SeanceMapper seanceMapper) {
        this.plannifDataApiClient = plannifDataApiClient;
        this.classeMapper = classeMapper;
        this.matiereMapper = matiereMapper;
        this.professeurMapper = professeurMapper;
        this.salleMapper = salleMapper;
        this.seanceMapper = seanceMapper;
    }

    public Planning buildPlanning() {
        Planning planning = new Planning();
        planning.setClasses(classeMapper.toEntityList(plannifDataApiClient.getClasses()));
        planning.setProfesseurs(professeurMapper.toEntityList(plannifDataApiClient.getProfesseurs()));
        planning.setMatieres(matiereMapper.toEntityList(plannifDataApiClient.getMatieres()));
        planning.setSalles(salleMapper.toEntityList(plannifDataApiClient.getSalles()));
        planning.setSeances(seanceMapper.toEntityList(plannifDataApiClient.getSeances()));

        return planning;
    }
}
