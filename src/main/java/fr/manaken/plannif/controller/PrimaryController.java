package fr.manaken.plannif.controller;

import fr.manaken.plannif.dto.EleveDTO;
import fr.manaken.plannif.dto.ProfesseurDTO;
import fr.manaken.plannif.dto.SeanceDTO;
import fr.manaken.plannif.service.EleveService;
import fr.manaken.plannif.service.ProfesseurService;
import fr.manaken.plannif.service.SeanceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PrimaryController {

    private final ProfesseurService professeurService;
    private final SeanceService seanceService;
    private final EleveService eleveService;


    public PrimaryController(ProfesseurService professeurService, SeanceService seanceService, EleveService eleveService) {
        this.professeurService = professeurService;
        this.seanceService = seanceService;
        this.eleveService = eleveService;
    }

    @GetMapping("/profs")
    public List<ProfesseurDTO> profs() {
        return professeurService.getProfesseurs();
    }

    @GetMapping("/seances")
    public List<SeanceDTO> seances() {
        return seanceService.getSeances();
    }

    @GetMapping("/eleves/{idClasse}")
    public List<EleveDTO> eleves(@PathVariable Long idClasse) {
        return eleveService.getEleves(idClasse);
    }
}
