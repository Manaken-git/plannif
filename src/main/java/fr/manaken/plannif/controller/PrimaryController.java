package fr.manaken.plannif.controller;

import fr.manaken.plannif.model.Professeur;
import fr.manaken.plannif.service.ProfesseurService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PrimaryController {

    private final ProfesseurService professeurService;

    public PrimaryController(ProfesseurService professeurService) {
        this.professeurService = professeurService;
    }

    @GetMapping("/test")
    public List<Professeur> test() {
        return professeurService.getProfesseurs();
    }
}
