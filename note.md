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
        Controller["PlanningController (Synchrone)"]
        JobController["PlanningJobController (Asynchrone & SSE)"]
        JobManager["PlanningJobManager (In-Memory Registry)"]
        Service["PlanningService"]
        SolverEngine["Timefold Solver Engine / SolverManager"]
        Exporter["PlanningExporter (HTML Dashboard)"]
        ApiClient["PlannifDataApiClient (RestClient)"]
    end

    subgraph DataService ["Microservice planning-data (:8081)"]
        DataApi["API REST CRUD & Base MariaDB"]
    end

    VueApp -->|POST /planning/solve| Controller
    VueApp -->|GET /planning/score/explain| Controller
    VueApp -->|POST /planning/jobs| JobController
    VueApp -->|GET /planning/jobs/{id}/progress (SSE)| JobController
    JobController --> JobManager
    JobManager --> SolverEngine
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
  - `planning.standard-session-duration-hours` : `2.0` (heures par défaut par séance lors de la génération)
  - `planning.solver.spent-limit-seconds` : `120` (temps max solver)

---

## 3. 🧩 Arborescence du Code Source

```
src/main/java/fr/manaken/plannif/
├── PlannifApplication.java           # Point d'entrée Spring Boot
├── business/                             # Cœur Timefold (Solution, Configuration & Contraintes)
│   ├── Planning.java                     # @PlanningSolution (Agrégat de résolution)
│   ├── PlanningConstraintConfiguration.java # @ConstraintConfiguration (Poids dynamiques des scores)
│   └── PlanningConstraints.java          # @ConstraintProvider (28 contraintes de score)
├── client/data/                          # Client REST vers planning-data
│   ├── PlannifDataApiClient.java         # RestClient Spring 6 pour tous les endpoints
│   ├── config/PlannifDataClientConfig.java # Configuration du Bean RestClient
│   └── dto/                              # DTOs (ClasseDto, SeanceDto, PlanningDto, etc.)
│   └── mapper/                           # Mappers MapStruct (entité <-> DTO)
├── config/
│   └── TimefoldSolverConfig.java         # Configuration Spring du SolverFactory et SolverManager
├── controller/
│   ├── PlanningController.java           # Endpoints REST synchrones (/planning/solve [GET/POST], /planning/score/explain [GET/POST], /planning/solve-html)
│   └── PlanningJobController.java        # Endpoints REST asynchrones (/planning/jobs [POST], /{id} [GET], /{id}/progress [SSE], /{id}/stop [POST])
├── exporter/
│   └── PlanningExporter.java             # Générateur de Dashboard HTML / diagramme de Gantt interactif
├── model/                                # Modèles du domaine
│   ├── Classe.java                       # Classe scolaire (presences, eleves, referents)
│   ├── ClassePresence.java               # Période de présence obligatoire [dateDebut, dateFin]
│   ├── ClasseReferent.java               # Association d'un professeur référent à une classe avec un rôle
│   ├── DistanceSalle.java                # Matrice des distances entre salles
│   ├── Eleve.java                        # Élève rattaché à une Classe
│   ├── Equipement.java                   # Matériel / ressource
│   ├── EquipementSalle.java              # Association Salle <-> Équipement
│   ├── Matiere.java                      # Discipline
│   ├── MatiereClasseConfig.java          # Volume horaire, période, typeSalleRequis et prioriteMatin pour une classe
│   ├── PlageHoraire.java                 # Plage horaire préférée prof
│   ├── Professeur.java                   # Enseignant (quotas heures max/jour, max/semaine, max/séance)
│   ├── ProfesseurDayOff.java             # Jour de repos enseignant (0=Lundi..4=Vendredi)
│   ├── ProfesseurIndisponibilite.java    # Indisponibilités horaires fines (jour, heureDebut, heureFin)
│   ├── RoleReferent.java                 # Enum PRINCIPAL, CO_REFERENT, TUTEUR_ENTREPRISE
│   ├── Salle.java                        # Salle de cours avec code, capacité, type
│   ├── Seance.java                       # @PlanningEntity (debut, salle, professeur, alignementId, typeSalleRequis)
│   ├── SemaineType.java                  # Enum SEMAINE_1, SEMAINE_2, SEMAINE_3
│   ├── TeacherClassWork.java             # Objet utilitaire Timefold pour calculs en minutes de charge prof/classe
│   └── Vacances.java                     # Plage de dates de vacances scolaires
└── service/
    ├── PlanningService.java              # Orchestration, génération dates/séances, sauvegarde
    └── job/                              # Gestion du cycle de vie asynchrone des jobs
        ├── JobStatus.java                # Enum SUBMITTED, SOLVING, COMPLETED, TERMINATED_EARLY, FAILED
        ├── PlanningJobManager.java       # Registry en mémoire, orchestration SolverManager & SSE
        └── PlanningJobState.java         # État thread-safe et métadonnées de progression d'un job
```

---

## 4. 🧠 Modèle de Résolution Timefold

### Entités & Variables
1. **Planning Solution** : `Planning`
   - Score type : `HardSoftScore` (pénalités Hard pour les impossibilités strictes, Soft pour le confort/optimisation).
   - Surcharge dynamique des poids : `ConstraintWeightOverrides<HardSoftScore> constraintWeightOverrides` (Timefold moderne).
   - `@PlanningEntityCollectionProperty` : `List<Seance> seances`
   - `@ProblemFactCollectionProperty` & `@ValueRangeProvider` :
     - `datesDebutPossibles` (id: `"dateDebutRange"`) : `List<LocalDateTime>`
     - `salles` (id: `"salleRange"`)
     - `professeurs` (id: `"professeurRange"`)
   - Autres Facts : `classes`, `matieres`, `professeurDayOffs`, `professeurIndisponibilites`, `distanceSalles`, `classePresences`, `matiereClasseConfigs`, `vacances`.

2. **Planning Entity** : `Seance`
   - Clé de planning : `@PlanningId Long id`
   - Variables de décision (attribuées par le solver) :
     - `@PlanningVariable(valueRangeProviderRefs = "dateDebutRange") LocalDateTime debut`
     - `@PlanningVariable(valueRangeProviderRefs = "salleRange") Salle salle`
     - `@PlanningVariable(valueRangeProviderRefs = "professeurRange") Professeur professeur`
   - Attributs calculés / fixes :
     - `fin` : calculé automatiquement à partir de `debut` et de la durée (`debut.plusMinutes(getDureeMinutes())`)
     - `dureeMinutes` / `dureeAttendueMinutes` : 90 min pour `TP`, 60 min pour les autres types (ou durée paramétrable personnalisée)
     - `typeSalleRequis` : type de salle attendu (direct ou hérité de `MatiereClasseConfig`)
     - `alignementId` : identifiant d'alignement pour synchronisation de groupes/options
     - `classe`, `matiere`, `type`

---

## 5. 📏 Catalogue Complet des Contraintes (`PlanningConstraints`)

Le solver évalue **30 contraintes** (14 Hard, 16 Soft / 17 Soft selon configuration), pilotées par `PlanningConstraintConfiguration` avec calculs optimisés en `long` minutes :

### 🔴 Contraintes Strictes (Hard Constraints)
| # | Nom | Méthode | Règle / Description |
|---|---|---|---|
| 1 | **Room Conflict** | `roomConflict` | Pas de chevauchement temporel de deux séances dans la même salle. |
| 2 | **Teacher Conflict** | `teacherConflict` | Un même enseignant ne peut pas animer deux séances en même temps. |
| 3 | **Student Group Conflict** | `studentGroupConflict` | Une même classe ne peut pas avoir deux séances simultanées. |
| 4 | **Student Group Presence** | `studentGroupPresence` | Toute séance d'une classe doit tomber dans sa période de présence (`ClassePresence`). |
| 5 | **Teacher Must Be Qualified** | `teacherMustBeQualified` | Le professeur assigné doit avoir la matière dans ses compétences. |
| 6 | **Subject Class Period** | `subjectClassPeriodConstraint` | La séance doit respecter la plage de dates définie dans `MatiereClasseConfig`. |
| 7 | **Holiday Conflict** | `holidayConflict` | Aucune séance ne peut avoir lieu pendant les vacances (`Vacances`). |
| 8 | **Vie de Classe Timing** | `vieDeClasseTimingConstraint` | Une séance `VIE_DE_CLASSE` doit être placée soit sur le 1er jour (9h-10h), soit sur le dernier jour (10h-11h). |
| 9 | **Vie de Classe Referent** | `vieDeClasseReferentConstraint` | Le professeur affecté à une séance `VIE_DE_CLASSE` doit obligatoirement figurer parmi les professeurs référents déclarés de la classe (`classe.getReferents()`). |
| 10 | **Seance Type & Duration Match** | `seanceTypeDurationMatch` | Vérifie la correspondance exacte entre la durée réelle de la séance et sa durée attendue (`dureeAttendueMinutes` ou par défaut 90 min pour TP, 60 min sinon). |
| 11 | **Room Capacity** | `roomCapacity` | Le nombre d'élèves de la classe (`classe.getEleves().size()`) ne doit pas dépasser la capacité de la salle (`salle.getCapacite()`). Pénalité proportionnelle au dépassement. |
| 12 | **Room Equipment & Type Match** | `roomEquipmentMatch` | La salle assignée doit correspondre au type de salle requis (`typeSalleRequis` de la séance ou de `MatiereClasseConfig`). |
| 13 | **Teacher Unavailability** | `teacherUnavailability` | Interdit la planification d'un professeur pendant ses indisponibilités fines (`ProfesseurIndisponibilite`). |
| 14 | **Aligned Sessions** | `alignedSessions` | Deux séances partageant le même `alignementId` doivent obligatoirement commencer sur le même créneau horaire. |

### 🟡 Contraintes Souples (Soft Constraints)
| # | Nom | Méthode | Règle / Description |
|---|---|---|---|
| 1 | **Teacher Day Off** | `teacherDayOff` | Ne pas planifier un professeur sur son jour de congé hebdomadaire. |
| 2 | **Teacher Max Hours / Day** | `teacherMaxHoursPerDay` | Pénalité proportionnelle en minutes au dépassement du quota journalier de l'enseignant. |
| 3 | **Teacher Max Hours / Week** | `teacherMaxHoursPerWeek` | Pénalité proportionnelle en minutes au dépassement du quota hebdomadaire de l'enseignant. |
| 4 | **Teacher Max Hours / Session** | `teacherMaxHoursPerSession` | Pénalité proportionnelle en minutes au dépassement de la durée max d'une séance pour l'enseignant. |
| 5 | **Teacher-Class 2-Days Limit** | `teacherClassMaxHoursOverTwoDays` | Limite à 5h (300 min) le volume d'enseignement pour une classe sur 2 jours consécutifs (pénalité proportionnelle). |
| 6 | **Teacher Max Gap** | `teacherMaxGap` | Évite les trous de plus de 2 heures (120 min) pour un enseignant (pénalité proportionnelle au temps excédentaire). |
| 7 | **Teacher Lunch Break** | `teacherLunchBreak` | Garantit au moins 45 minutes de pause repas pour l'enseignant entre 11h30 et 14h00. |
| 8 | **Teacher Travel Distance** | `teacherTravelDistance` | Pénalise l'enchaînement immédiat de deux cours dans des bâtiments distants (> 200m via `DistanceSalle`). |
| 9 | **Student Lunch Break** | `studentLunchBreak` | Garantit au moins 60 minutes de pause méridienne pour chaque classe entre 11h30 et 14h00 (pénalité proportionnelle au déficit). |
| 10 | **Student Max Hours / Day** | `studentMaxHoursPerDay` | Plafonne à 7h (420 min) le volume journalier de cours pour les élèves. |
| 11 | **Student Max Gap** | `studentMaxGap` | Minimise les heures creuses au milieu de la journée de la classe (hors pause déjeuner). |
| 12 | **Max Sessions / Day / Subject** | `subjectClassMaxSessionsPerDay` | Maximum 1 séance d'une même matière par jour pour une classe donnée. |
| 13 | **Subject Class Spreading** | `subjectClassSpreading` | Évite de planifier la même matière deux jours consécutifs pour favoriser l'espacement pédagogique. |
| 14 | **High Cognitive Load** | `highCognitiveLoad` | Privilégie le matin (avant 12h30) pour les matières à forte charge cognitive (`MatiereClasseConfig.prioriteMatin == true`). |
| 15 | **Session Pairing** | `sessionPairing` | Favorise le regroupement de deux séances d'1h en bloc continu de 2h le même jour plutôt que de les éclater. |
| 16 | **Student Day Start Time** | `studentDayStartTime` | Verrouille le début de la journée de cours d'une classe à 8h00 (ou 9h00 le lundi) ; pénalise proportionnellement au retard en minutes tout démarrage plus tardif. Poids configurable via `ConstraintWeightOverrides`. |
| 17 | **Vie de Classe Principal Referent** | `vieDeClassePrincipalReferentPreference` | Privilégie l'attribution de la séance `VIE_DE_CLASSE` au professeur référent ayant le rôle `PRINCIPAL` par rapport à un co-référent. |

---

## 6. ⚙️ Logique Métier & Algorithmes Clés (`PlanningService`)

### 1. `buildPlanning()`
- Appelle `PlannifDataApiClient` pour récupérer l'ensemble des données de référence.
- Initialise les listes de relations bidirectionnelles (`ClassePresence`, `ProfesseurDayOff`, `ProfesseurIndisponibilite`, `Vacances`, `DistanceSalle`).
- Ré-associe en mémoire les objets référencés par ID (`Classe`, `Professeur`, `Matiere`, `Salle`).
- Récupère les débuts candidats si disponibles depuis l'API ou génère les dates de début possibles dans `Planning.datesDebutPossibles`.

### 2. `generateDatesDebutPossiblesIfNeeded(Planning planning)`
- Parcourt chaque `ClassePresence` (jours ouvrés lundi à vendredi, hors vacances) :
  - **Créneaux 1h standard** :
    - Matin : 8h (sauf lundi), 9h, 10h, 11h
    - Après-midi (sauf vendredi) : 13h, 14h, 15h, 16h
  - **Créneaux 1h30 (TP)** :
    - Matin : 8h (sauf lundi), 9h30, 11h
    - Après-midi (sauf vendredi) : 13h, 14h30, 16h

### 3. `generateSeancesIfNeeded(Planning planning)`
- À partir de `MatiereClasseConfig` :
  - Calcule `count = round(volumeHorairePeriode / standardSessionDurationHours)`.
  - Instancie les `Seance` avec `classe` et `matiere` assignées, `professeur`, `salle` et `debut` non initialisés (laissés au Solver).
- Gestion des **Vie de Classe** :
  - Génère **2 séances `VIE_DE_CLASSE`** par période de présence de la classe : une séance pour le **premier jour (09h00 - 10h00)** ET une séance pour le **dernier jour (10h00 - 11h00)**.
  - Réutilise les séances `VIE_DE_CLASSE` non planifiées existantes ou crée de nouvelles séances avec la matière auto-générée `"Vie de classe"`.

---

## 7. 🧪 Suite de Tests & Qualité
 
La suite comprend désormais **69 tests unitaires et d'intégration** (100% au vert, 0 échec, 0 erreur) :
- `ConstraintVerifierTest` (**32 tests**) : Validation isolée de chaque contrainte Timefold (anciennes et nouvelles, y compris le démarrage à 8h00 / 9h00 le lundi) et validation de la configuration dynamique des poids.
- `PlanningJobControllerTest` (**10 tests**) : Validation de la soumission asynchrone non-bloquante, cycle de vie des jobs, streaming SSE temps réel, explication de score, surcharge dynamique de poids de contraintes (`constraintWeightOverrides`) et arrêt anticipé avec gestion des conflits 409.
- `SolverTest` (**3 tests**) : 
  - Résolution globale avec assertion sur la faisabilité (`isFeasible() == true`).
  - Immutabilité stricte des séances épinglées (`@PlanningPin boolean pinned`) face au solveur et réallocation des séances mobiles sans conflit.
  - Surcharge dynamique des poids de contraintes (`ConstraintWeightOverrides`) multipliant fidèlement les pénalités appliquées par le moteur de score.
- `AdvancedSolverTest` (4 tests) : Validation du calcul de score et détection des conflits (salle, prof, présence classe).
- `PlanningControllerTest` (5 tests) : Test de l'orchestration globale, chargement de scénarios JSON, génération dynamique, persistance et propagation de la date/heure de fin (`fin`), et export Gantt HTML.
- `PlannifDataApiClientTest` (8 tests) : Validation des appels HTTP avec MockRestServiceServer.
- `MappersTest` (6 tests) : Validation des mappers MapStruct (dont mapping direct de `debut`, `fin` et `pinned` dans `SeanceDto`).
- `PlannifApplicationTests` (1 test) : Chargement du contexte Spring Boot.
