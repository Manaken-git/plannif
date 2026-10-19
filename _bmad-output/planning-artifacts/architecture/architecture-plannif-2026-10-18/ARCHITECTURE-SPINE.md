---
name: plannif-async-architecture
type: architecture-spine
purpose: build-substrate
altitude: feature
paradigm: Layered Architecture with Reactive In-Memory Job Management
scope: Asynchronous execution, SSE streaming, entity pinning, dynamic scoring and lean persistence hand-off
status: final
created: 2026-10-18
updated: 2026-10-18
binds:
  - CAP-1
  - CAP-2
  - CAP-3
  - CAP-4
  - CAP-5
sources:
  - ../../../specs/spec-plannif-async-and-pinning/SPEC.md
  - ../../../specs/spec-plannif-async-and-pinning/api-contract.md
companions: []
---

# Architecture Spine — Plannif (Moteur Asynchrone & Pinning)

## Design Paradigm

Architecture en couches découplée (*Layered Architecture*) combinée à un orchestrateur de jobs réactif en mémoire (*Reactive In-Memory Job Orchestrator*) :
* **Web / REST** (`controller`) : Exposition non bloquante des commandes (`POST /planning/jobs`), flux streaming (`GET /planning/jobs/{jobId}/progress`) et interruptions (`POST /planning/jobs/{jobId}/stop`).
* **Job & Solver Management** (`service.job`, `service`) : Orchestration des cycles de vie via `SolverManager`, découplage des threads et publication d'événements vers les abonnés `SseEmitter`.
* **Domain & Timefold Core** (`business`, `model`) : Entités Timefold (`Planning`, `Seance`), contraintes de score et gestion du verrouillage (`@PlanningPin`).
* **External Client** (`client.data`) : Persistance immédiate dans `planning-data` à la terminaison.

```mermaid
graph TD
    Client["Client (Vue.js / REST)"]
    
    subgraph PlannifBackend ["Microservice Plannif"]
        JobController["PlanningJobController (REST & SSE)"]
        JobManager["PlanningJobManager (In-Memory State & Emitters)"]
        PlanningService["PlanningService (Data Assembly & Persistence)"]
        SolverEngine["Timefold SolverManager (Thread Pool)"]
    end
    
    subgraph DataService ["Microservice planning-data"]
        ApiClient["PlannifDataApiClient"]
        MariaDB[("MariaDB")]
    end
    
    Client -->|1. POST /jobs| JobController
    Client -->|2. GET /jobs/{id}/progress (SSE)| JobController
    JobController --> JobManager
    JobManager -->|solveAndListen| SolverEngine
    SolverEngine -.->|bestSolutionConsumer| JobManager
    JobManager -.->|emit score-update| Client
    SolverEngine -->|finalBestSolutionConsumer| PlanningService
    PlanningService --> ApiClient
    ApiClient --> MariaDB
```

---

## Invariants & Rules

### AD-1 [ADOPTED] — Timefold `SolverManager` as Asynchronous Computation Engine

* **Binds:** `CAP-1`, `CAP-3`, `service.job`
* **Prevents:** Blocage des threads de requêtes HTTP servlet et concurrence anarchique sur le CPU lors de résolutions simultanées.
* **Rule:** Tout calcul de planning soumis via l'API doit s'exécuter de façon asynchrone via un bean Spring `SolverManager<Planning, String>`. La méthode synchrone `solver.solve(...)` est strictement interdite dans les contrôleurs de production et réservée aux tests unitaires (`ConstraintVerifierTest`, `SolverTest`).

### AD-2 [ADOPTED] — Reactive Progress Streaming via `SseEmitter`

* **Binds:** `CAP-2`, `controller`
* **Prevents:** Saturation de la bande passante et latence induite par le polling HTTP intensif.
* **Rule:** Le contrôleur enregistre un `SseEmitter` avec un timeout configurable (par défaut égal à `solverSpentLimitSeconds + 30s`). Chaque appel au callback `bestSolutionConsumer` du `SolverManager` pousse un événement `score-update` non bloquant (`CompletableFuture`). Si un client se déconnecte, l'émetteur est retiré sans interrompre la résolution du solver.

### AD-3 [ADOPTED] — Lean Lifecycle & Direct Persistence Hand-off

* **Binds:** `CAP-2`, `CAP-3`, `service`
* **Prevents:** Épuisement de la mémoire heap (`OutOfMemoryError`) causé par l'accumulation d'arbres de graphes d'objets `Planning` résolus en mémoire.
* **Rule:** Lorsque `finalBestSolutionConsumer` est invoqué (par fin normale ou appel à `solverManager.terminateEarly(jobId)`), la solution est immédiatement transmise à `plannifDataApiClient.savePlanning(solution)`. L'instance `Planning` en mémoire est immédiatement déréférencée et seuls l'ID persistant `savedPlanningId` et le score final sont conservés dans l'état du job.

### AD-4 [ADOPTED] — Entity Immutability through `@PlanningPin`

* **Binds:** `CAP-4`, `model.Seance`
* **Prevents:** Déplacement ou réassignation non désirée par Timefold de cours manuellement positionnés.
* **Rule:** L'entité `Seance` doit comporter un attribut `boolean pinned` annoté `@PlanningPin`. Lorsque `pinned == true`, Timefold garantit l'immutabilité des variables `@PlanningVariable` (`debut`, `salle`, `professeur`) durant toutes les phases de recherche (Construction Heuristic et Local Search), tout en évaluant l'impact des contraintes vis-à-vis des autres séances.

### AD-5 [ADOPTED] — Dynamic Weight Injection via `ConstraintWeightOverrides`

* **Binds:** `CAP-5`, `business.Planning`
* **Prevents:** Figeage rigide des poids de contraintes nécessitant des redéploiements de code pour adapter les politiques pédagogiques.
* **Rule:** Les surcharges de poids transmises dans le corps de la requête sont converties en `ConstraintWeightOverrides<HardSoftScore>` et positionnées sur l'entité `@PlanningSolution Planning` avant soumission à `SolverManager`.

---

## Consistency Conventions

| Préoccupation | Convention |
| :--- | :--- |
| **Identifiants de Job** | Chaîne UUID v4 (`UUID.randomUUID().toString()`) générée à la soumission. |
| **Statuts de Job** | Enum `JobStatus { SUBMITTED, SOLVING, COMPLETED, TERMINATED_EARLY, FAILED }`. |
| **Format des Scores** | Représentation textuelle canonique `HardSoftScore` (ex. `0hard/-42soft`). |
| **Événements SSE** | Format standardisé : `event: score-update`, `event: completed`, `event: error`. |
| **Gestion des Erreurs** | Exception interceptée par le handler `SolverManager` renseignant `errorMessage` et basculant le statut en `FAILED`. |

---

## Stack

| Composant | Version | Rôle |
| :--- | :--- | :--- |
| **Java** | 25 | Langage d'exécution runtime |
| **Spring Boot** | 3.5.6 | Framework applicatif et Web MVC (`SseEmitter`) |
| **Timefold Solver** | 1.27.0 | Moteur de résolution (`timefold-solver-core`, `SolverManager`) |
| **MapStruct** | 1.6.3 | Mapping DTO / Entités de domaine |
| **Lombok** | 1.18.42 | Réduction du code boilerplate |

---

## Structural Seed

```text
src/main/java/fr/manaken/plannif/
├── business/
│   ├── Planning.java
│   ├── PlanningConstraintConfiguration.java
│   └── PlanningConstraints.java
├── config/
│   └── SolverManagerConfig.java              # Configuration du Bean SolverManager Spring
├── controller/
│   ├── PlanningController.java               # Endpoints synchrones & legacy
│   └── PlanningJobController.java            # Endpoints asynchrones (POST /jobs, SSE progress, STOP)
├── model/
│   ├── Seance.java                           # Ajout de @PlanningPin boolean pinned
│   └── ...
├── service/
│   ├── PlanningService.java
│   └── job/
│       ├── PlanningJobManager.java           # Gestionnaire d'état, registry en mémoire & SSE
│       ├── PlanningJobState.java             # État léger d'un job (id, status, score, emitters)
│       └── JobStatus.java                    # Enum des statuts
```

---

## Capability → Architecture Map

| Capacité | Composant responsable | Règle / Décision directrice |
| :--- | :--- | :--- |
| **CAP-1** (Soumission Asynchrone) | `PlanningJobController`, `PlanningJobManager` | `AD-1` (`SolverManager.solveAndListen`) |
| **CAP-2** (Streaming SSE) | `PlanningJobController`, `SseEmitter` | `AD-2` (Émission d'événements `score-update`) |
| **CAP-3** (Arrêt Manuel & Sauvegarde) | `PlanningJobManager`, `PlanningService` | `AD-1`, `AD-3` (`terminateEarly` & `savePlanning`) |
| **CAP-4** (Pinning de Séances) | `model.Seance` | `AD-4` (`@PlanningPin boolean pinned`) |
| **CAP-5** (Surcharge Dynamique) | `PlanningService`, `Planning` | `AD-5` (`ConstraintWeightOverrides`) |

---

## Deferred

* **Clustering multi-nœuds du solveur** : Reporté à une phase ultérieure car le volume actuel de requêtes d'un établissement scolaire est parfaitement absorbé par le pool de workers en mémoire d'une unique instance Spring Boot.
* **Persistance de l'historique complet des exécutions SSE** : Seul le planning final résultant est persisté en base de données relationnelle (`planning-data`) ; l'enregistrement détaillé milliseconde par milliseconde des scores intermédiaires n'est pas conservé une fois le job clos.
