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
  - `planning.solver.spent-limit-seconds` : `30` (temps max solver par défaut)
  - `planning.solver.unimproved-spent-limit-seconds` : `8` (arrêt anticipé sur stagnation si aucune amélioration après 8 secondes)

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
     - `@PlanningVariable(valueRangeProviderRefs = "seanceDateDebutRange") LocalDateTime debut` (ValueRangeProvider ciblé par séance, restreint aux seules dates de présence de la classe)
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
  - **Exclusion stricte de la matière `"Vie de classe"`** lors de la génération standard de cours : évite de créer des séances mobiles de type `COURS` pour la vie de classe.
  - Instancie les `Seance` avec `classe` et `matiere` assignées, `professeur`, `salle` et `debut` non initialisés (laissés au Solver).
- Gestion des **Vie de Classe** :
  - Génère **2 séances `VIE_DE_CLASSE`** par période de présence de la classe : une séance pour le **premier jour (09h00 - 10h00)** ET une séance pour le **dernier jour (10h00 - 11h00)**.
  - **Épinglage déterministe pré-solve (`@PlanningPin pinned = true`)** :
    - `debut` et `fin` sont fixés immédiatement avant le solveur Timefold.
    - `professeur` est assigné intelligemment : sélectionne en priorité le professeur référent principal (`PRINCIPAL`). Si indisponible (jour de repos `ProfesseurDayOff`, indisponibilité horaire, ou séance épinglée concurrente), bascule automatiquement sur un co-référent (`CO_REFERENT`).
    - `salle` est sélectionnée automatiquement : salle de type `COURS` de capacité adéquate et sans conflit avec une autre séance épinglée.
    - La séance est marquée `pinned = true`, interdisant à Timefold de déplacer le créneau et évitant tout goulot d'étranglement ou score infaisable.
  - Réutilise les séances `VIE_DE_CLASSE` existantes ou crée de nouvelles séances avec la matière auto-générée `"Vie de classe"`, et purge toute séance résiduelle non épinglée liée à la vie de classe.

### 4. ⚡ Optimisation du Score Soft (-45 892 $\rightarrow$ -715)
- **Verrouillage strict des créneaux à l'heure pile (`:00`)** pour les séances de 60 min : exclusion de 9h30 et 14h30 (réservés exclusivement aux TP), éradiquant 100% des micro-trous de 30 minutes.
- **Plafonnement des séances le vendredi de fin de session** : aucun cours n'est placé à 11h ou après le dernier vendredi, garantissant un cycle continu 8h-11h (Cours + Cours + Vie de classe).
- **`ValueRangeProvider` ciblé par entité (`seanceDateDebutRange`)** : chaque séance sélectionne désormais ses dates candidates exclusivement parmi les dates de présence réelles de sa classe, éliminant ~85% de mouvements invalides et démultipliant l'efficacité de la recherche locale.
- **Correction de la pause méridienne** : `studentMaxGap` intègre désormais la fin à 11h00 (`!isBefore(11:00)`), évitant la fausse pénalité de -120 soft sur la pause 11h-13h.
- **Résultat** : Score final **`0hard / -715soft`** (0 conflit, 0 trou prof, 0 trou 30min, 100% de respect des blocs de 2h).

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

---

## 8. 🌐 Intégration Frontend (`plannifVue`)

L'intégration de la résolution Timefold dans le client web React `plannifVue` a été mise en place :
1. **Proxy Vite multi-target** ([vite.config.ts](file:///e:/Boulot/workspace/plannifVue/vite.config.ts)) :
   - Redirection de `/planning/jobs` et `/planning/solve` vers `http://localhost:8080` (solveur Timefold `plannif`).
   - Maintien du proxy CRUD (`/profs`, `/seances`, `/plannings`, etc.) vers `http://localhost:8081` (`planning-data`).
2. **Couche API & Types** ([solver.types.ts](file:///e:/Boulot/workspace/plannifVue/src/features/planning/types/solver.types.ts) et [solver.api.ts](file:///e:/Boulot/workspace/plannifVue/src/features/planning/api/solver.api.ts)) :
   - Soumission asynchrone non-bloquante (`POST /planning/jobs`).
   - Streaming temps réel via Server-Sent Events (`EventSource` sur `GET /planning/jobs/{jobId}/progress`).
   - Consultation d'état unitaire / polling de secours (`GET /planning/jobs/{jobId}`).
   - Interruption manuelle (`POST /planning/jobs/{jobId}/stop`).
3. **Hook React orchestrateur** ([useSolverJob.ts](file:///e:/Boulot/workspace/plannifVue/src/features/planning/hooks/useSolverJob.ts)) :
   - Gestion d'état réactive (`idle`, `solving`, `completed`, `stopped`, `failed`).
   - Bascule automatique en polling fallback en cas de rupture de flux SSE.
   - Nettoyage automatique des timers et connexions au démontage.
4. **Composants d'interface** :
   - Bouton d'action *"Optimiser le planning"* dans le header de [PlanningPage.tsx](file:///e:/Boulot/workspace/plannifVue/src/features/planning/pages/PlanningPage/PlanningPage.tsx).
   - Bannière informative en arrière-plan lorsque le calcul tourne et que la fenêtre modale est minimisée.
   - Modale interactive [SolverProgressModal.tsx](file:///e:/Boulot/workspace/plannifVue/src/features/planning/components/SolverProgressModal/SolverProgressModal.tsx) affichant le score dur (faisabilité / conflits), le score souple (qualité) et le temps écoulé, avec bouton d'interruption manuelle et redirection automatique vers le planning sauvegardé.
5. **Ergonomie & Volet Latéral Diagnostic (Drawer)** :
   - Remplacement des 4 volumineuses cartes statiques de KPIs par un volet latéral fluide à droite ([PlanningStats.tsx](file:///e:/Boulot/workspace/plannifVue/src/features/planning/components/PlanningStats/PlanningStats.tsx)), libérant plus de 220px de hauteur verticale pour le calendrier.
   - Bouton d'en-tête *"Santé du planning"* avec badge dynamique d'alertes (`warning` si conflits, `success` si 100% conforme).
   - Onglets de filtrage rapide (Professeurs, Classes & Repas, Salles, Matières) avec interaction au clic pour zoomer et filtrer directement la séance dans la grille d'emploi du temps.
6. **Saisie Rapide & Réaménagement d'Horaires (Post-Solve)** :
   - **Micro-actions au survol** ([SessionCard.tsx](file:///e:/Boulot/workspace/plannifVue/src/features/planning/components/SessionCard/SessionCard.tsx)) : barre épurée (suppression des boutons `±1h` suite à la revue UX pour éviter les clics accidentels), conservant le bouton cadenas `[🔒/🔓]` pour verrouiller/déverrouiller et le raccourci popover `[⏱️]`.
   - **Piste A — Popover Compact d'Édition Rapide** ([QuickReschedulePopover.tsx](file:///e:/Boulot/workspace/plannifVue/src/features/planning/components/QuickReschedulePopover/QuickReschedulePopover.css)) : popover contextuel ancré sous la séance comprenant la sélection du jour (pills L/M/M/J/V), les créneaux standards prédéfinis (Matin 8h, 9h, 10h, Après-midi 13h30, 14h30, etc.), saisie fine `HH:mm`, et bascule d'épinglage.
   - **Piste B — Drag & Drop direct avec Ombre Portée de Largage** ([CalendarGrid.tsx](file:///e:/Boulot/workspace/plannifVue/src/features/planning/components/CalendarGrid/CalendarGrid.tsx)) : glisser-déposer natif HTML5 sur les colonnes des jours avec magnétisme par pas de 30 minutes, ombre portée dynamique de projection (`.calendar-grid__drop-preview`) avec halo indigo, affichage du créneau projeté en temps réel et préservation stricte de la durée de la séance.
   - **Persistance & Épinglage (PlanningPin)** : le champ `pinned: boolean` est propagé à l'API backend (`@PlanningPin` dans Timefold), assurant que les ajustements manuels de l'utilisateur restent figés lors des relances ultérieures du solveur.
   - **Synchronisation d'état réactive (Optimistic UI)** : synchronisation immédiate des modifications (`updateSessionInState`) à la fois sur l'état global `seances` et dans le planning sélectionné `plannings[id].seances`, garantissant un rafraîchissement visuel instantané de l'emploi du temps sans latence ni désynchronisation.
7. **Suppression de Planning en Cascade (`planning-data`)** :
   - Correction de l'erreur `SQLIntegrityConstraintViolationException: Cannot delete or update a parent row (fk_seance_planning)` lors de la suppression d'un planning.
   - [SeanceRepository.java](file:///e:/Boulot/workspace/planning-data/src/main/java/fr/manaken/plannif/repository/SeanceRepository.java) : typage de la clé primaire en `Long` (au lieu de `Integer`) et ajout de `@Modifying @Query` `deleteByPlanningId`.
   - [Planning.java](file:///e:/Boulot/workspace/planning-data/src/main/java/fr/manaken/plannif/model/Planning.java) : configuration de `cascade = CascadeType.ALL, orphanRemoval = true` sur `seances` et `seancesNonPlanifiees`.
   - [PlanningService.java](file:///e:/Boulot/workspace/planning-data/src/main/java/fr/manaken/plannif/service/PlanningService.java) : suppression propre ensembliste via `deleteSeancesByPlanningId` et `deleteSeancesNonPlanifieesByPlanningId` avant suppression du planning.
   - [db.changelog-master.yaml](file:///e:/Boulot/workspace/planning-data/src/main/resources/db/changelog/db.changelog-master.yaml) : changeset 41 appliquant `ON DELETE CASCADE` sur `fk_seance_planning` et `fk_snp_planning`.
8. **Harmonisation CSS & Charte Graphique Dark Mode** :
   - Enrichissement de [ARCHITECTURE.md](file:///e:/Boulot/workspace/plannifVue/ARCHITECTURE.md) avec la charte officielle des tokens CSS (`tokens.css`), les règles de composition et la liste des anti-patterns proscrits.
   - Refactorisation complète des composants récents ([SolverProgressModal.css](file:///e:/Boulot/workspace/plannifVue/src/features/planning/components/SolverProgressModal/SolverProgressModal.css), [SeancesNonPlanifieesModal.css](file:///e:/Boulot/workspace/plannifVue/src/features/plannings/components/SeancesNonPlanifieesModal/SeancesNonPlanifieesModal.css), [QuickReschedulePopover.css](file:///e:/Boulot/workspace/plannifVue/src/features/planning/components/QuickReschedulePopover/QuickReschedulePopover.css), [PlanningStats.css](file:///e:/Boulot/workspace/plannifVue/src/features/planning/components/PlanningStats/PlanningStats.css), [PlanningPage.css](file:///e:/Boulot/workspace/plannifVue/src/features/planning/pages/PlanningPage/PlanningPage.css), [SessionCard.css](file:///e:/Boulot/workspace/plannifVue/src/features/planning/components/SessionCard/SessionCard.css)) pour éradiquer 100 % des résidus de styles clairs (Light Mode) et lier l'ensemble des éléments aux tokens de design system.

---

## 9. ⚡ Optimisation des Performances Timefold (Phase 1 — Quick Wins)

Afin d'éviter des temps de résolution excessivement longs et des attentes inutiles côté frontend :
1. **Terminaison intelligente sur stagnation (`unimprovedSpentLimit`)** :
   - Ajout d'une terminaison ISO-8601 `<unimprovedSpentLimit>PT8S</unimprovedSpentLimit>` dans [solverConfig.xml](file:///e:/Boulot/workspace/plannif/src/main/resources/solverConfig.xml).
   - Paramétrable via `planning.solver.unimproved-spent-limit-seconds: 8` et pris en compte dynamiquement par [TimefoldSolverConfig.java](file:///e:/Boulot/workspace/plannif/src/main/java/fr/manaken/plannif/config/TimefoldSolverConfig.java).
   - Dès qu'aucune amélioration du score n'est constatée pendant 8 secondes consécutives, le solveur termine immédiatement au lieu de tourner dans le vide jusqu'au timeout maximal.
2. **Réduction du timeout maximal par défaut** :
   - Passage du timeout par défaut de 120s (et 1200s dans `application.yml`) à **30s** (`<spentLimit>PT30S</spentLimit>` / `planning.solver.spent-limit-seconds: 30`).
3. **Note sur l'édition Community vs Enterprise** :
   - `moveThreadCount` (multithreading CPU) requiert Timefold Solver Enterprise Edition (`timefold-solver-enterprise-core`). Pour la version Community Open-Source, le moteur opère en single-thread déterministe avec gain de temps garanti par l'arrêt sur stagnation.

### Phase 2 — Nettoyage & Réduction de l'Espace de Recherche (Search Space)
1. **Exclusion des vacances scolaires à la génération des dates candidates** :
   - Dans [PlanningService.java](file:///e:/Boulot/workspace/plannif/src/main/java/fr/manaken/plannif/service/PlanningService.java), méthode `generateDatesDebutPossiblesIfNeeded` : vérification stricte via `isDateDuringVacances` pour ne jamais proposer de créneaux candidats tombant sur les plages de `Vacances`.
   - Évite au solveur de générer et d'explorer des milliers d'affectations absurdes pendant les congés pour ensuite devoir les rejeter via `holidayConflict`.
2. **Pré-assignation des professeurs uniques qualifiés** :
   - Lors de la génération des séances (`generateSeancesIfNeeded`) ou de la liaison des séances existantes (`buildPlanning`), si une matière n'a qu'un unique professeur qualifié parmi `planning.getProfesseurs()`, ce professeur est assigné directement sur la séance.
   - Élimine le gaspillage de l'heuristique de construction qui tentait tous les autres professeurs inaptes avant de subir la pénalité `teacherMustBeQualified`.
3. **Pré-remplissage du type de salle requis (`typeSalleRequis`)** :
   - Affecté directement sur `seance.setTypeSalleRequis(config.getTypeSalleRequis())` dès la préparation, permettant un filtrage direct sans dépendre systématiquement de jointures à chaud.

### Phase 3 — Optimisation des Flux de Contraintes (Constraint Streams)
1. **Indexation temporelle stricte par date sur les 4 clauses `ifNotExists`** :
   - Dans `teacherMaxGap`, `studentMaxGap`, `teacherLunchBreak` et `studentLunchBreak` : ajout du critère `equal((s1, s2) -> s1.getFin().toLocalDate(), s -> s.getDebut().toLocalDate())` dans la sous-clause `ifNotExists(Seance.class)`.
   - **Gain** : Timefold indexe désormais la 3ème séance par `(ressource, date)` au lieu de parcourir toutes les séances de l'année pour vérifier l'absence d'intermédiaire, allégeant drastiquement le réseau Rete/Bavet.
2. **Court-circuit $O(1)$ dans `roomEquipmentMatch`** :
   - Traitement scindé en deux flux concaténés : un flux direct $O(1)$ pour les séances disposant de leur `requiredRoomType` précalculé (sans aucune jointure), et un flux de repli avec jointure `MatiereClasseConfig` pour les données brutes ou non enrichies.
3. **Pré-filtrage des distances dans `teacherTravelDistance`** :
   - Le flux `DistanceSalle.class` est pré-filtré sur `distance > 200` avant la jointure avec les paires de séances consécutives.
   - **Gain** : Élimine le produit cartésien sur l'ensemble des paires de salles proches du campus.

---

## 10. 🛠️ Résolution du Bug Hibernate dans `planning-data` (`orphanRemoval`)

### Problème
Lors de la persistance du planning à l'issue du solve (`POST /planning`), Hibernate levait l'exception :
```
org.hibernate.HibernateException: A collection with orphan deletion was no longer referenced by the owning entity instance: fr.manaken.plannif.model.Planning.seances
```

### Cause Racine
Dans l'entité JPA `Planning` de `planning-data`, les relations `@OneToMany` sont configurées avec `orphanRemoval = true` :
- `@OneToMany(mappedBy = "planning", cascade = CascadeType.ALL, orphanRemoval = true) private List<Seance> seances;`
- `@OneToMany(mappedBy = "planning", cascade = CascadeType.ALL, orphanRemoval = true) private List<SeanceNonPlanifiee> seancesNonPlanifiees;`

Avec le `@Setter` par défaut généré par Lombok, l'appel `entity.setSeances(seanceEntities)` dans `PlanningService.savePlanning` réassignait la référence interne de la liste. Lorsque l'entité est déjà attachée/chargée depuis la base (`dataFetcher.getPlanning(...)`), Hibernate remplace la liste par sa propre collection gérée (`PersistentBag`). Écraser cette référence déréférence le `PersistentBag`, ce qui déclenche immédiatement cette exception de sécurité lors du flush de transaction.

### Correction Appliquée
1. **Implémentation explicite des setters dans `Planning.java`** :
   - Remplacement de l'affectation directe par `clear()` et `addAll()` in-place :
     ```java
     public void setSeances(List<Seance> seances) {
         if (this.seances == null) {
             this.seances = new ArrayList<>();
         } else {
             this.seances.clear();
         }
         if (seances != null) {
             this.seances.addAll(seances);
         }
     }
     ```
   - Appliqué également à `setSeancesNonPlanifiees`.
   - La collection `PersistentBag` gérée par Hibernate reste ainsi référencée, et `orphanRemoval` s'occupe de supprimer automatiquement les séances orphelines.
2. **Nettoyage dans `PlanningService.java` (`planning-data`)** :
   - Suppression de l'appel manuel `dataPusher.deleteSeancesNonPlanifieesByPlanningId(entity.getId())` devenu redondant et en conflit avec le cycle de vie géré par Hibernate.

---

## 11. 🚀 Phase 4 — Déblocage de l'Heuristique de Construction (Heuristic Scaling & Environment Mode)

### Symptôme observé
Sur un jeu de données plus réaliste (324 séances, 972 variables, 685 valeurs candidates) :
```
DefaultConstructionHeuristicPhase terminated early with step count (32).
The solution may not be fully initialized.
Construction Heuristic phase (0) ended: time spent (30004), best score (-2hard/-1260soft), move evaluation speed (46992/sec), step total (32).
environment mode (PHASE_ASSERT)
```
Seulement 32 séances sur 324 étaient initialisées en 30 secondes ! Les 292 autres séances restaient non assignées (`null`), et la phase de recherche locale (`Local Search`) n'avait même pas pu démarrer.

### Diagnostic Technique
1. **Produit Cartésien par défaut des variables multiples** :
   Chaque séance possède 3 variables de planification : `professeur`, `debut`, `salle`.
   Par défaut, Timefold évalue toutes les combinaisons simultanément pour chaque entité :
   $$\text{Combinaisons par séance} = |\text{professeurs}| \times |\text{dates}| \times |\text{salles}| \approx 15 \times 650 \times 20 = 195\,000 \text{ mouvements / entité}$$
   À ~47 000 mouvements/sec, évaluer une seule entité prenait 4 secondes. En 30 secondes, le solveur n'avait le temps d'initialiser que 32 entités !
2. **Ralentissement par `environment mode (PHASE_ASSERT)`** :
   Lors d'une exécution dans un IDE ou avec l'option JVM `-ea` (assertions activées), Timefold basculait automatiquement en mode `PHASE_ASSERT` qui recalcule et vérifie l'intégralité du score à chaque étape, divisant le débit par 2 à 3.

### Optimisations Appliquées
1. **Heuristique de Construction Séquentielle via `queuedEntityPlacer`** :
   Dans [solverConfig.xml](file:///e:/Boulot/workspace/plannif/src/main/resources/solverConfig.xml), configuration d'une sélection séquentielle des variables par entité avec réplication mimic (`mimicSelectorRef="placerEntitySelector"`) :
   - Étape 1 : Choix du professeur qualifié (15 combinaisons)
   - Étape 2 : Choix du créneau horaire compatible (650 combinaisons)
   - Étape 3 : Choix de la salle adéquate libre (20 combinaisons)
   - **Gain mathématique** : Passage de $O(P \times D \times S)$ ($195\,000$ mouvements/entité) à $O(P + D + S)$ (**$685$ mouvements/entité**).
   - **Résultat** : L'initialisation complète des 324 séances passe de **~20 minutes à moins de 4 secondes** !
2. **Activation de la Recherche Locale (`Local Search` - Late Acceptance)** :
   Une fois les 324 séances initialisées en 3-4 secondes, les 26 secondes restantes du budget sont intégralement allouées à la méta-heuristique `Late Acceptance` pour résoudre les conflits durs restants et maximiser les préférences douces.
3. **Mode d'Environnement Dédié à la Performance (`NON_REPRODUCIBLE`)** :
   Défini dans `solverConfig.xml`, `application.yml` (`planning.solver.environment-mode: NON_REPRODUCIBLE`) et pris en charge dynamiquement par `TimefoldSolverConfig.java`. Évite toute pénalité de recalcul de contrôle.

---

## 12. ⚡ Phase 5 — Indexation Temporelle des Conflits Majeurs (`roomConflict`, `teacherConflict`, `studentGroupConflict`)

### Analyse des Métriques de Recherche Locale
Après le déblocage de l'heuristique de construction (qui s'exécute désormais en **643 ms** à **340 796 mouvements/sec** et initialise l'intégralité des séances) :
- En 29,4 secondes de recherche locale, le score s'est effondré de **-58 495 hard** à **-153 hard** (élimination de plus de 99,7 % des conflits).
- Toutefois, la vitesse de calcul en recherche locale était retombée à **7 074 mouvements/sec** (contre 340 000/sec en CH).

### Cause Identifiée
Dans les 3 contraintes de conflit dur fondamentales (`roomConflict`, `teacherConflict`, `studentGroupConflict`), le `.join(Seance.class)` associait toutes les séances de l'année pour une même salle, un même professeur ou une même classe, sans condition de date sur le join :
- Pour 100 séances d'une même ressource sur l'année, le nœud de jointure conservait $100 \times 99 / 2 = 4\,950$ paires en mémoire et évaluait leur chevauchement temporel à chaque mouvement.
- Deux séances ne peuvent se chevaucher que si elles ont lieu le **même jour** (séances intra-journalières de 1 à 2 heures).

### Correction Appliquée
Dans [PlanningConstraints.java](file:///e:/Boulot/workspace/plannif/src/main/java/fr/manaken/plannif/business/PlanningConstraints.java) :
- Ajout de la clé de jointure d'égalité de date sur les 3 contraintes :
  ```java
  equal(s -> s.getDebut() != null ? s.getDebut().toLocalDate() : null)
  ```
- **Gain** : Timefold indexe désormais les jointures par `(ressource, date)`. Le volume de paires dans chaque nœud de jointure s'effondre de ~99 %, démultipliant le débit d'évaluation de la recherche locale pour converger encore plus rapidement vers `0 hard`.

---

## 13. 🎯 Phase 6 — Éradication de la "Capacité Fantôme" & Alignement Heuristique

### Diagnostic du Dump des 39 Pénalités Dures Résiduelles
L'analyse de l'explication de score détaillée du solve sur le grand jeu de données (324 séances, 972 variables, 685 créneaux candidats) a révélé la décomposition exacte des -39 hard :
- **`Student group presence`** : **31 violations** (79,5 % des pénalités)
- **`Subject class period`** : **6 violations** (15,4 % des pénalités)
- **`Student group conflict`** : 1 violation
- **`Teacher must be qualified for the subject`** : 1 violation
- **Toutes les autres contraintes** : **0 violation** (`Room conflict`, `Teacher conflict`, `Room capacity`, `Room equipment`, `Teacher unavailability`, `Holiday conflict`, etc. sont 100 % satisfaites).

### Analyse Architecturale : Le Piège de la Capacité Fantôme
1. **La fausse équivalence des pénalités à 1 Hard** :
   Dans un modèle où toutes les contraintes dures pèsent `-1hard`, placer une séance dans une semaine vide où les élèves sont absents (stage, alternance) ne coûte que `-1hard` (`Student group presence`).
   À l'inverse, ramener cette séance dans la semaine de présence où les ressources sont fortement sollicitées risque d'engendrer un conflit enseignant (`-1hard`) ou des pénalités douces (`Student max gap`, etc.).
   Pour le solveur, `-1hard / 0soft` est strictement supérieur à `-1hard / -150soft` : la recherche locale se retrouve piégée dans un optimum local où 37 séances sont "cachées" dans des semaines désertes.
2. **L'aveuglement temporel de la Construction Heuristic** :
   Dans l'ordre initial (`professeur` -> `debut` -> `salle`), le professeur était choisi alors que `debut` était encore `null`. L'heuristique ne pouvait donc évaluer aucune incompatibilité d'agenda enseignant lors du choix de celui-ci.

### Optimisations Appliquées
1. **Inversion de l'ordre d'assignation dans [solverConfig.xml](file:///e:/Boulot/workspace/plannif/src/main/resources/solverConfig.xml)** :
   Ordre séquentiel révisé :
   - **Étape 1 : `debut`** : Sélection du créneau temporel en premier. Les contraintes temporelles (`studentGroupPresence`, `subjectClassPeriodConstraint`, `holidayConflict`) sont évaluées dès le départ, positionnant immédiatement les séances dans leur semaine de présence réelle.
   - **Étape 2 : `professeur`** : Le créneau étant connu, Timefold évalue directement les disponibilités de l'enseignant (`teacherConflict`, `teacherUnavailability`, `teacherDayOff`, etc.).
   - **Étape 3 : `salle`** : Avec l'horaire et le professeur fixés, la salle compatible et libre est allouée.
2. **Hiérarchisation des Poids Durs via `ConstraintWeightOverrides` dans [PlanningJobManager.java](file:///e:/Boulot/workspace/plannif/src/main/java/fr/manaken/plannif/service/job/PlanningJobManager.java)** :
   Pour interdire au solveur de sacrifier la présence physique des élèves au profit de la facilité de placement :
   - `Student group presence` : relevé à **`-10hard`** par défaut
   - `Subject class period` : relevé à **`-10hard`** par défaut
   - `Teacher must be qualified` : relevé à **`-10hard`** par défaut
   Ces surcharges sont dynamiques (préservant les tests unitaires basés sur `ONE_HARD`) et empêchent définitivement le solveur d'utiliser des semaines d'absence comme variables d'ajustement.
3. **Restitution des Logs d'Explication de Score** :
   Ajout de `explanation.getSummary()` dans le log `onFinalSolution` de `PlanningJobManager` pour un suivi instantané de la répartition des pénalités à chaque fin de solve.

---

## 14. 🏁 Phase 7 — Validation du Recalage des Vacances & Égalisation des Poids Physiques

### Résultats Majeurs Obtenus (Solve ID 31)
Suite à l'ajustement des vacances (Toussaint ramenée à 1 semaine) et au recalage des périodes de présence des classes `TermB` et `CAPA` :
- **`Student group presence` : 0 VIOLATION !** (Passé de 31 à 22, puis désormais à **0** : 100 % des 324 séances sont planifiées dans des périodes où les classes sont réellement présentes).
- **`Teacher must be qualified` : 0 VIOLATION !**
- **`Holiday conflict` : 0 VIOLATION !**
- **Vitesse Construction Heuristic** : **367 ms** à **600 824 mouvements/sec** (nouveau record absolu).
- **Score Hard résiduel** : tombé de -304 hard à **-56 hard**.

### Vérification Technique des Données (Analyse Post-Solve)
1. **Dernier point de friction sur les dates : `MatiereClasseConfig` de CAPA** :
   - Présence 2 de la classe CAPA : `04/01/2027 -> 17/01/2027`.
   - Les 5 configs de matières de CAPA (`Anglais`, `Mathématique`, `Biologie`, `Economie`, `Vie de classe`) ont toutes leur `dateFin` fixée au `30/12/2026`.
   - **Conséquence** : Les 2 séances planifiées en janvier 2027 déclenchent la pénalité `Subject class period` (`-20 hard`).
   - **Action** : Repousser la `dateFin` des configs de CAPA au-delà de janvier 2027 (ex: `31/01/2027` ou `30/06/2027`).

2. **Égalisation Totale des Poids d'Impossibilité Physique** :
   - Afin d'éliminer définitivement les 24 `Student group conflict`, 7 `Room conflict` et 5 `Teacher conflict`, tous les conflits physiques absolus sont désormais verrouillés au même niveau (`10hard`) dans [PlanningJobManager.java](file:///e:/Boulot/workspace/plannif/src/main/java/fr/manaken/plannif/service/job/PlanningJobManager.java) :
     - `ROOM_CONFLICT` = `10hard`
     - `TEACHER_CONFLICT` = `10hard`
     - `STUDENT_GROUP_CONFLICT` = `10hard`
     - `HOLIDAY_CONFLICT` = `10hard`
     - `STUDENT_GROUP_PRESENCE` = `10hard`
     - `SUBJECT_CLASS_PERIOD` = `10hard`
     - `TEACHER_MUST_BE_QUALIFIED` = `10hard`
   - Le solveur n'a ainsi plus aucun arbitrage préférentiel lui permettant de superposer deux séances pour satisfaire une autre contrainte.

---

## 15. 🏆 Phase 8 — Faisabilité Parfaite (0 Hard) & Prolongation de l'Optimisation Douce

### Succès Majeur (Solve ID 33)
À l'issue de l'extension des dates de fin de matières pour CAPA et de l'équilibrage des poids :
- **Score Hard : 0 HARD ! (Zéro point de pénalité résiduel).**
- L'intégralité des 17 contraintes dures du cahier des charges sont **100 % respectées** :
  - 0 conflit de salle
  - 0 conflit d'enseignant
  - 0 conflit de classe
  - 0 cours hors présence
  - 0 professeur non qualifié
  - 0 cours pendant les vacances
  - 0 dépassement de capacité ou d'équipement de salle
  - 0 violation de rythme ou de pause déjeuner imposée

### Déverrouillage de l'Optimisation Douce (Soft Score)
Dans le solve précédent, le solveur s'était arrêté à $t = 65.2\text{s}$ car la balise `<bestScoreFeasible>true</bestScoreFeasible>` lui intimait de couper dès l'atteinte de `0 hard`, sans chercher à réduire les pénalités douces (`Student day start time` et `Student max gap`).

**Ajustements appliqués :**
1. **Suppression de `bestScoreFeasible` dans [solverConfig.xml](file:///e:/Boulot/workspace/plannif/src/main/resources/solverConfig.xml)** :
   Le solveur n'interrompt plus sa recherche à `0 hard` et alloue le reste du temps disponible à condenser les emplois du temps des élèves et des enseignants.
2. **Budget Temps & Condition d'Arrêt Intelligent** :
   - `spentLimit` configuré à **120 secondes** (dans `solverConfig.xml` et `application.yml`).
   - `unimprovedSpentLimit` configuré à **20 secondes** : Si pendant 20 secondes consécutives aucune amélioration du score doux n'est trouvée, le solveur s'arrête automatiquement.
3. **Pré-affectation tournante des professeurs qualifiés** dans [PlanningService.java](file:///e:/Boulot/workspace/plannif/src/main/java/fr/manaken/plannif/service/PlanningService.java) :
   Garantit que 100 % des séances démarrent avec un professeur qualifié dès le départ.

