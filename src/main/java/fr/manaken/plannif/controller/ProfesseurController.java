package fr.manaken.plannif.controller;

import fr.manaken.plannif.dto.ProfesseurDTO;
import fr.manaken.plannif.service.ProfesseurService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/profs")
public class ProfesseurController {
    private final ProfesseurService professeurService;

    public ProfesseurController(ProfesseurService professeurService) {
        this.professeurService = professeurService;
    }

    @GetMapping("/list")
    public List<ProfesseurDTO> profs() {
        return professeurService.getProfesseurs();
    }

    @PostMapping("/create")
    public ProfesseurDTO createProf(@RequestBody ProfesseurDTO p) {
        return professeurService.saveProfesseur(p);
    }
}
