package fr.manaken.plannif.controller;

import fr.manaken.plannif.dto.ProfesseurDTO;
import fr.manaken.plannif.dto.SeanceDTO;
import fr.manaken.plannif.service.ProfesseurService;
import fr.manaken.plannif.service.SeanceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PrimaryController {

    private final ProfesseurService professeurService;
    private final SeanceService seanceService;


    public PrimaryController(ProfesseurService professeurService, SeanceService seanceService) {
        this.professeurService = professeurService;
        this.seanceService = seanceService;
    }

    @GetMapping("/profs")
    public List<ProfesseurDTO> profs() {
        return professeurService.getProfesseurs();
    }

    @GetMapping("/seances")
    public List<SeanceDTO> seances() {
        return seanceService.getSeances();
    }
}
