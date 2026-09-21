# 📋 Notes d'Architecture et Synthèse Technique — Projet `plannif`

> **Note persistante pour l'agent IA et les développeurs** : Ce document constitue la référence technique consolidée du projet `plannif`. Il permet de comprendre immédiatement le fonctionnement, les règles métier, les contraintes Timefold, les flux de données et la structure du code sans avoir à re-parser l'ensemble du projet.

---

## 1. 📌 Identité & Rôle du Projet

- **Nom du projet** : `plannif` (Package racine : `fr.manaken.plannif`)
- **Type** : Microservice Spring Boot dédié au calcul d'optimisation combinatoire et à la génération d'emplois du temps scolaires.
- **Moteur d'optimisation** : [Timefold Solver](https://timefold.ai/) `1.27.0` (ex-OptaPlanner).
- **Stack technique** :
  - **Java** : version 25
  - **Spring Boot** : 3.5.6 (Web, Starter Test)
  - **Timefold Solver** : `timefold-solver-core` & `timefold-solver-test` (1.27.0)
  - **MapStruct** : 1.6.3 (Binding Lombok-MapStruct `0.2.0`)
  - **Lombok** : 1.18.42
  - **Build Tool** : Maven (`pom.xml`)

---

## 2. 🌐 Architecture & Écosystème

Le projet `plannif` s'intègre dans une architecture multi-services :

```mermaid
graph LR
    subgraph Frontend
        VueApp["Frontend Vue.js (Vite :5173)"]
    end

    subgraph Plannif ["Microservice plannif (:8080)"]
        Controller["PlanningController"]
        Service["PlanningService"]
        SolverEngine["Timefold Solver Engine"]
        Exporter["PlanningExporter (HTML Dashboard)"]
        ApiClient["PlannifDataApiClient (RestClient)"]
    end

    subgraph DataService ["Microservice planning-data (:8081)"]
        DataApi["API REST CRUD & Base MariaDB"]
    end

    VueApp -->|POST /planning/solve| Controller
    Controller --> Service
    Service -->|GET /classes, /profs, etc.| ApiClient
    ApiClient --> DataApi
    Service --> SolverEngine
    SolverEngine --> Service
    Service -->|POST /plannings/save| ApiClient
    Service --> Exporter
```

### Configuration réseau (`application.yml`)
- Port serveur `plannif` : `8080`
- URL service distant `plannif-data` : `http://localhost:8081/planning-data` (`plannif-data.api.base-url`)
- Paramètres métier configurables :
  - `planning.teaching-weeks` : `36`
  - `planning.standard-session-duration-hours` : `2.0` (heures par défaut par séance lors de la génération)

---

## 3. 🧩 Arborescence du Code Source

```
src/main/java/fr/manaken/plannif/
├── PlannifApplication.java               # Point d'entrée Spring Boot
├── business/                             # Cœur Timefold (Solution & Contraintes)
│   ├── Planning.java                     # @PlanningSolution (Agrégat de résolution)
│   └── PlanningConstraints.java          # @ConstraintProvider (18 contraintes de score)
├── client/data/                          # Client REST vers planning-data
│   ├── PlannifDataApiClient.java         # RestClient Spring 6 pour tous les endpoints
│   ├── config/PlannifDataClientConfig.java # Configuration du Bean RestClient
│   ├── dto/                              # 12 DTOs (ClasseDto, SeanceDto, PlanningDto, etc.)
│   └── mapper/                           # 10 Mappers MapStruct (entité <-> DTO)
├── controller/
│   └── PlanningController.java           # Endpoints REST (/planning/solve, /planning/solve-html)
├── exporter/
│   └── PlanningExporter.java             # Générateur de Dashboard HTML / diagramme de Gantt interactif
├── model/                                # Modèles du domaine
│   ├── Classe.java                       # Classe scolaire, liste presences, méthode needsVieDeClasse
│   ├── ClassePresence.java               # Période de présence obligatoire [dateDebut, dateFin]
│   ├── Creneau.java                      # Créneau horaire [debut, fin, semaineType]
│   ├── DistanceSalle.java                # Matrice des distances entre salles
│   ├── Eleve.java                        # Élève rattaché à une Classe
│   ├── Equipement.java                   # Matériel / ressource
│   ├── EquipementSalle.java              # Association Salle <-> Équipement
│   ├── Matiere.java                      # Discipline enseignée
│   ├── MatiereClasseConfig.java          # Volume horaire et période obligatoire d'une matière pour une classe
│   ├── PlageHoraire.java                 # Plage horaire préférée prof
│   ├── Professeur.java                   # Enseignant (quotas heures max/jour, max/semaine, max/séance)
│   ├── ProfesseurDayOff.java             # Jour de repos enseignant (0=Lundi..4=Vendredi)
│   ├── Salle.java                        # Salle de cours avec code, capacité, type
│   ├── Seance.java                       # @PlanningEntity (Unité élémentaire planifiée)
│   ├── SemaineType.java                  # Enum SEMAINE_1, SEMAINE_2, SEMAINE_3
│   ├── TeacherClassWork.java             # Objet utilitaire Timefold pour calculs de charge prof/classe
│   └── Vacances.java                     # Plage de dates de vacances scolaires
└── service/
    └── PlanningService.java              # Orchestration, génération créneaux/séances, sauvegarde
```

---

## 4. 🧠 Modèle de Résolution Timefold

### Entités & Variables
1. **Planning Solution** : `Planning`
   - Score type : `HardSoftScore` (pénalités Hard pour les impossibilités strictes, Soft pour le confort/optimisation).
   - `@PlanningEntityCollectionProperty` : `List<Seance> seances`
   - `@ProblemFactCollectionProperty` & `@ValueRangeProvider` :
     - `creneaux` (id: `"creneauRange"`)
     - `salles` (id: `"salleRange"`)
     - `professeurs` (id: `"professeurRange"`)
   - Autres Facts : `classes`, `matieres`, `professeurDayOffs`, `classePresences`, `matiereClasseConfigs`, `vacances`.

2. **Planning Entity** : `Seance`
   - Clé de planning : `@PlanningId Long id`
   - Variables de décision (attribuées par le solver) :
     - `@PlanningVariable(valueRangeProviderRefs = "professeurRange") Professeur professeur`
     - `@PlanningVariable(valueRangeProviderRefs = "salleRange") Salle salle`
     - `@PlanningVariable(valueRangeProviderRefs = "creneauRange") Creneau creneau`
   - Attributs fixes : `classe`, `matiere`, `type` (`COURS`, `TP`, `EXAMEN`, `VIE_DE_CLASSE`).

---

## 5. 📏 Catalogue Complet des Contraintes (`PlanningConstraints`)

Le solver évalue **18 contraintes** (10 Hard, 8 Soft) :

### 🔴 Contraintes Strictes (Hard Constraints - 1 Hard point par violation)
| # | Nom | Méthode | Règle / Description |
|---|---|---|---|
| 1 | **Room Conflict** | `roomConflict` | Pas de chevauchement temporel de deux séances dans la même salle. |
| 2 | **Teacher Conflict** | `teacherConflict` | Un même enseignant ne peut pas animer deux séances en même temps. |
| 3 | **Student Group Conflict** | `studentGroupConflict` | Une même classe ne peut pas avoir deux séances simultanées. |
| 4 | **Student Group Presence** | `studentGroupPresence` | Toute séance d'une classe doit obligatoirement tomber dans une période où la classe est présente (`ClassePresence`). |
| 5 | **Teacher Must Be Qualified** | `teacherMustBeQualified` | Le professeur assigné à la séance doit avoir la matière dans ses compétences (`prof.getMatieres().contains(matiere)`). Exception pour `VIE_DE_CLASSE`. |
| 6 | **Subject Class Period** | `subjectClassPeriodConstraint` | Si un module a une plage de dates définie dans `MatiereClasseConfig`, la séance doit être comprise entre `dateDebut` et `dateFin`. |
| 7 | **Holiday Conflict** | `holidayConflict` | Aucune séance ne peut avoir lieu pendant une période de vacances (`Vacances`). |
| 8 | **Student Group Week Type Mismatch** | `studentGroupWeekTypeMismatch` | La semaine type du créneau (`SEMAINE_1`, `SEMAINE_2`, `SEMAINE_3`) doit correspondre à l'index de semaine calculé depuis le début de présence de la classe. |
| 9 | **Vie de Classe Timing** | `vieDeClasseTimingConstraint` | Une séance `VIE_DE_CLASSE` doit obligatoirement être placée soit le **premier lundi (09h00 - 10h00)**, soit le **dernier vendredi (10h00 - 11h00)** de la période de présence. |
| 10 | **Seance Type & Duration Match** | `seanceTypeDurationMatch` | Une séance de type `TP` doit durer exactement **90 minutes**, les autres types ne doivent pas durer 90 minutes (créneaux standard 1h). |

### 🟡 Contraintes Souples (Soft Constraints - 1 Soft point par violation)
| # | Nom | Méthode | Règle / Description |
|---|---|---|---|
| 1 | **Teacher Day Off** | `teacherDayOff` | Ne pas planifier un professeur sur son jour de congé hebdomadaire (`ProfesseurDayOff`, 0=Lundi..4=Vendredi). |
| 2 | **Teacher Max Hours / Day** | `teacherMaxHoursPerDay` | Ne pas dépasser le quota d'heures par jour du professeur (`maxHeuresParJour`). |
| 3 | **Teacher Max Hours / Week** | `teacherMaxHoursPerWeek` | Ne pas dépasser le quota d'heures par semaine du professeur (`maxHeuresParSemaine`). |
| 4 | **Teacher Max Hours / Session** | `teacherMaxHoursPerSession` | Ne pas dépasser la durée max par séance du professeur (`maxHeuresParSeance`). |
| 5 | **Teacher-Class 2-Days Limit** | `teacherClassMaxHoursConsecutive` | Un professeur ne doit pas donner plus de 5 heures de cours à une même classe sur 2 jours consécutifs. |
| 6 | **Teacher Max Gap** | `teacherMaxGap` | Éviter les trous de plus de 2 heures (120 min) dans l'emploi du temps d'une même journée pour un professeur. |
| 7 | **Max Sessions / Day / Subject** | `subjectClassMaxSessionsPerDay` | Maximum 1 séance d'une même matière par jour pour une classe donnée. |
| 8 | **Subject Class Spreading** | `subjectClassSpreading` | Éviter de planifier la même matière deux jours consécutifs (distance <= 1 jour) pour favoriser l'espacement pédagogique. |

---

## 6. ⚙️ Logique Métier & Algorithmes Clés (`PlanningService`)

### 1. `buildPlanning()`
- Appelle `PlannifDataApiClient` pour récupérer l'ensemble des données de référence.
- Initialise les listes de relations bidirectionnelles (`ClassePresence`, `ProfesseurDayOff`, `Vacances`).
- Ré-associe en mémoire les objets référencés par ID (hydratation des graphes d'objets `Classe`, `Professeur`, `Matiere`, `Salle`, `Creneau`).
- Génère automatiquement les créneaux et séances si la base est vide.

### 2. `generateCreneauxIfNeeded(Planning planning)`
- Parcourt chaque `ClassePresence` (jours ouvrés lundi à vendredi) :
  - **Créneaux 1h standard** :
    - Matin : 8h-9h (sauf lundi), 9h-10h, 10h-11h, 11h-12h
    - Après-midi (sauf vendredi) : 13h-14h, 14h-15h, 15h-16h, 16h-17h
  - **Créneaux 1h30 (TP)** :
    - Matin : 8h-9h30 (sauf lundi), 9h30-11h, 11h-12h30
    - Après-midi (sauf vendredi) : 13h-14h30, 14h30-16h, 16h-17h30
  - Calcule automatiquement l'indice `SemaineType` (`daysBetween / 7 + 1`).

### 3. `generateSeancesIfNeeded(Planning planning)`
- À partir de `MatiereClasseConfig` :
  - Calcule `count = round(volumeHorairePeriode / standardSessionDurationHours)`.
  - Instancie les `Seance` avec `classe` et `matiere` assignées, `professeur`, `salle` et `creneau` non initialisés (laissés au Solver).
- Gestion des **Vie de Classe** :
  - Appelle `classe.needsVieDeClasse(presence, vacances)`.
  - Réutilise les séances `VIE_DE_CLASSE` non planifiées existantes ou crée de nouvelles séances avec la matière auto-générée `"Vie de classe"`.

---

## 7. 🧪 Suite de Tests & Qualité

La suite comprend **35 tests unitaires et d'intégration** (tous au vert) :
- `ConstraintVerifierTest` (12 tests) : Teste isolément chaque contrainte Timefold via `ConstraintVerifier`.
- `AdvancedSolverTest` (4 tests) : Validation du calcul de score et détection des conflits (salle, prof, présence classe).
- `SolverTest` (1 test) : Test d'intégration de résolution complète avec assertion sur la faisabilité (`isFeasible() == true`).
- `PlanningControllerTest` (3 tests) : Test de l'orchestration globale, chargement de scénarios JSON (`test_big_data_scenario.json`), génération dynamique et export Gantt HTML.
- `PlannifDataApiClientTest` (8 tests) : Validation des appels HTTP avec MockRestServiceServer.
- `MappersTest` (6 tests) : Validation des mappers MapStruct.
- `PlannifApplicationTests` (1 test) : Chargement du contexte Spring Boot.

---

## 8. 🔍 Points d'Attention & Opportunités d'Amélioration

1. **Modèles dormants / non exploités dans les contraintes actuelles** :
   - `DistanceSalle` / `distance` : Le calcul du temps de trajet inter-salles entre deux cours consécutifs n'est pas encore transformé en contrainte Timefold.
   - `Equipement` / `EquipementSalle` : Le filtrage de salle selon les équipements requis par une matière n'est pas encore actif dans `PlanningConstraints`.
   - `PlageHoraire` / `plageHorairePreferee` : Le souhait horaire du professeur n'est pas encore évalué dans les contraintes soft.
   - `Eleve` : Présent dans le modèle mais les affectations se font actuellement au niveau `Classe`.

2. **Performance & Terminaison Timefold** :
   - `solverConfig.xml` définit une limite de 120s (`secondsSpentLimit`), tandis que `PlanningController` surcharge programmatiquement avec `withTerminationSpentLimit(Duration.ofSeconds(30))`. Une harmonisation via configuration centralisée `application.yml` est recommandée.

3. **Gestion des exceptions dans l'API Client** :
   - Dans `PlanningService.buildPlanning()`, la récupération des vacances capture génériquement `Exception` sans logging structuré.

4. **Compatibilité Java 25 & Lombok/Mockito** :
   - Le projet tourne sous Java 25 avec avertissement sur le chargement dynamique d'agent ByteBuddy pour Mockito (à prévoir en argument JVM `-XX:+EnableDynamicAgentLoading` pour les futures versions de JDK).
