package fr.manaken.plannif.controller;

import fr.manaken.plannif.dto.EleveDTO;
import fr.manaken.plannif.dto.SeanceDTO;
import fr.manaken.plannif.service.EleveService;
import fr.manaken.plannif.service.SeanceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PrimaryController {

    private final SeanceService seanceService;
    private final EleveService eleveService;


    public PrimaryController(SeanceService seanceService, EleveService eleveService) {
        this.seanceService = seanceService;
        this.eleveService = eleveService;
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
