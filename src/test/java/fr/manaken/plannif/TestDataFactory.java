package fr.manaken.plannif;

import fr.manaken.plannif.business.Planning;
import fr.manaken.plannif.model.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TestDataFactory {

    public static Planning generateProblem() {
        Planning planning = new Planning();

        // Creneau
        Creneau c1 = new Creneau();
        c1.setId(1L);
        c1.setDebut(LocalTime.of(8, 0));
        c1.setFin(LocalTime.of(9, 0));

        List<Creneau> creneaux = new ArrayList<>();
        creneaux.add(c1);
        planning.setCreneaux(creneaux);

        // Salles
        Salle s1 = new Salle();
        s1.setId(1L);
        s1.setCode("A101");

        Salle s2 = new Salle();
        s2.setId(2L);
        s2.setCode("A102");

        List<Salle> salles = new ArrayList<>();
        salles.add(s1);
        salles.add(s2);
        planning.setSalles(salles);

        // Profs
        Professeur p1 = new Professeur();
        p1.setId(1L);
        p1.setNom("Prof1");

        Professeur p2 = new Professeur();
        p2.setId(2L);
        p2.setNom("Prof2");

        // Classes
        Classe cl1 = new Classe();
        cl1.setId(1L);
        cl1.setNom("Class1");

        Classe cl2 = new Classe();
        cl2.setId(2L);
        cl2.setNom("Class2");

        // Matieres
        Matiere m1 = new Matiere();
        m1.setId(1L);
        m1.setNom("Maths");

        p1.getMatieres().add(m1);
        p2.getMatieres().add(m1);

        // Seances
        Seance seance1 = new Seance();
        seance1.setId(1L);
        seance1.setProfesseur(p1);
        seance1.setClasse(cl1);
        seance1.setMatiere(m1);
        seance1.setCreneau(c1);

        Seance seance2 = new Seance();
        seance2.setId(2L);
        seance2.setProfesseur(p2);
        seance2.setClasse(cl2);
        seance2.setMatiere(m1);
        seance2.setCreneau(c1);

        List<Seance> seances = new ArrayList<>();
        seances.add(seance1);
        seances.add(seance2);
        planning.setSeances(seances);

        planning.setProfesseurDayOffs(new ArrayList<>());
        planning.setClassePresences(new ArrayList<>());

        return planning;
    }
}
