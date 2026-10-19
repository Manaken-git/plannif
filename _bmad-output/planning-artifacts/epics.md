---
status: final
stepsCompleted:
  - step-01-validate-prerequisites
  - step-02-design-epics
  - step-03-create-stories
  - step-04-final-validation
inputDocuments:
  - _bmad-output/specs/spec-plannif-async-and-pinning/SPEC.md
  - _bmad-output/specs/spec-plannif-async-and-pinning/api-contract.md
  - _bmad-output/planning-artifacts/architecture/architecture-plannif-2026-10-18/ARCHITECTURE-SPINE.md
---

# plannif - Epic Breakdown

## Overview

This document provides the complete epic and story breakdown for plannif, decomposing the requirements from the SPEC and Architecture Spine requirements into implementable stories.

## Requirements Inventory

### Functional Requirements

- **FR1**: Soumission asynchrone non-bloquante d'un job de résolution (`POST /planning/jobs`) retournant un `jobId` unique (`202 Accepted`).
- **FR2**: Streaming de progression en temps réel via Server-Sent Events (`GET /planning/jobs/{jobId}/progress`) émettant chaque nouveau meilleur score intermédiaire (`score-update`) et un événement terminal (`completed`/`terminated`).
- **FR3**: Consultation ponctuelle d'état (polling de secours `GET /planning/jobs/{jobId}`) fournissant le statut, le score actuel et le détail des pénalités.
- **FR4**: Interruption manuelle du calcul (`POST /planning/jobs/{jobId}/stop`) arrêtant immédiatement le solveur sous 1 seconde en préservant la meilleure solution calculée.
- **FR5**: Verrouillage / Pinning manuel de séances (`@PlanningPin boolean isPinned()` sur `Seance`) garantissant l'immutabilité des variables de décision pour les séances marquées.
- **FR6**: Surcharge dynamique des poids de contraintes (`ConstraintWeightOverrides`) injectée à la volée dans la requête pour adapter les priorités sans modifier le code.
- **FR7**: Persistance directe de la solution finale dans `planning-data` (`savePlanning`) avec libération immédiate de la mémoire heap à l'issue de la résolution.

### NonFunctional Requirements

- **NFR1 (Performance)**: Réponse immédiate (< 200ms) lors de la soumission `POST /planning/jobs`.
- **NFR2 (Non-régression)**: Maintien à 100% de succès sur la suite des 53 tests unitaires et d'intégration existants.
- **NFR3 (Gestion Mémoire)**: Aucun cache mémoire lourd des solutions terminées ; déréférencement immédiat de l'objet `Planning` après sauvegarde.
- **NFR4 (Réactivité d'arrêt)**: Interruption effective du solver sous 1 seconde via `solverManager.terminateEarly`.
- **NFR5 (Concurrence)**: Gestion propre d'un pool de calcul concurrent (3 à 5 résolutions parallèles).

### Additional Requirements

- **Starter / Base existante**: Application Spring Boot 3.5.6 / Java 25 existante avec Timefold 1.27.0.
- **Architecture technique**: Configuration d'un Bean Spring `SolverManager<Planning, String>` (AD-1).
- **Gestionnaire SSE**: Composant `PlanningJobManager` gérant les abonnés `SseEmitter` et la résilience aux déconnexions (AD-2).
- **Cycle de vie**: Hand-off direct vers `PlannifDataApiClient.savePlanning` sans conservation d'historique lourd en mémoire (AD-3).
- **Modèle de données**: Ajout de `@PlanningPin boolean pinned` sur l'entité `Seance` et prise en compte dans les DTOs/Mappers (AD-4).
- **Injection de poids**: Conversion des surcharges de poids de l'API vers `ConstraintWeightOverrides<HardSoftScore>` (AD-5).

### UX Design Requirements

*(Non applicable dans ce lot backend ; le contrat d'API est prêt pour l'intégration du frontend Vue.js)*

### FR Coverage Map

- **FR1**: Epic 1 (Story 1.1) — Soumission asynchrone non-bloquante avec génération de `jobId`
- **FR2**: Epic 1 (Story 1.2) — Streaming temps réel de progression via flux SSE (`score-update` & fin)
- **FR3**: Epic 1 (Story 1.3) — Consultation de statut et métriques de progression (polling fallback)
- **FR4**: Epic 1 (Story 1.4) — Arrêt manuel réactif préservant la meilleure solution intermédiaire
- **FR5**: Epic 2 (Story 2.1) — Verrouillage / Pinning de séances imposées via `@PlanningPin`
- **FR6**: Epic 2 (Story 2.2) — Surcharge dynamique des poids de contraintes par requête
- **FR7**: Epic 1 (Story 1.4) — Persistance automatique dans `planning-data` et libération mémoire

## Epic List

### Epic 1: Résolution Asynchrone, Streaming SSE & Persistance Sans Cache
Permettre aux utilisateurs et à l'interface de lancer des calculs d'optimisation en arrière-plan sans blocage HTTP, de suivre l'évolution continue du score en temps réel via un flux SSE, et d'interrompre le calcul à tout moment avec persistance immédiate de la meilleure solution dans la base de données sans encombrer la mémoire vive.
**FRs couverts :** FR1, FR2, FR3, FR4, FR7

### Epic 2: Contrôle Pédagogique Interactif — Séances Verrouillées (Pinning) & Poids Dynamiques
Permettre aux planificateurs scolaires d'imposer des cours et salles fixes (séances verrouillées via `@PlanningPin`) et d'ajuster dynamiquement les priorités pédagogiques relatives (surcharges de poids de contraintes) afin de tester différents scénarios d'emploi du temps sans recompiler l'application.
**FRs couverts :** FR5, FR6

---

## Epic 1: Résolution Asynchrone, Streaming SSE & Persistance Sans Cache

Permettre aux utilisateurs et à l'interface de lancer des calculs d'optimisation en arrière-plan sans blocage HTTP, de suivre l'évolution continue du score en temps réel via un flux SSE, et d'interrompre le calcul à tout moment avec persistance immédiate de la meilleure solution dans la base de données sans encombrer la mémoire vive.

### Story 1.1: Configuration du SolverManager & Soumission Asynchrone de Job (`POST /planning/jobs`)

As an API client / frontend,  
I want to submit a schedule solving request asynchronously and receive a unique `jobId`,  
So that the HTTP thread is not blocked and the long-running optimization task is delegated to a background worker pool.

**Acceptance Criteria:**

**Given** un problème de planning valide préparé ou chargé depuis `planning-data`,  
**When** un client envoie une requête `POST /planning/jobs`,  
**Then** le système retourne immédiatement un code HTTP `202 Accepted` avec un payload JSON contenant `jobId` (UUID v4), `status: "SOLVING"`, `submittedAt` et `solverSpentLimitSeconds`,  
**And** la tâche de résolution est transmise au `SolverManager<Planning, String>` sans bloquer le thread de requête HTTP (temps de réponse < 200ms),  
**And** un état de job initial est enregistré dans le registre en mémoire (`PlanningJobManager`).

---

### Story 1.2: Streaming de Progression Temps Réel via Server-Sent Events (`GET /planning/jobs/{jobId}/progress`)

As an API client / frontend user,  
I want to subscribe to a Server-Sent Events stream for a specific jobId,  
So that I can observe score improvements in real-time as the solver explores candidate schedules.

**Acceptance Criteria:**

**Given** un job de résolution en cours avec un `jobId` existant,  
**When** un client se connecte à `GET /planning/jobs/{jobId}/progress` avec l'en-tête `Accept: text/event-stream`,  
**Then** une connexion HTTP SSE permanente (`SseEmitter`) est établie,  
**And** à chaque nouvelle meilleure solution trouvée par le solveur (`bestSolutionConsumer`), un événement `score-update` est émis contenant le score actuel, le temps écoulé et les composantes hard/soft,  
**And** lorsque la résolution se termine, un événement terminal `completed` est émis avec le score final et le `savedPlanningId`, puis le flux SSE se ferme proprement,  
**And** si le client coupe prématurément la connexion SSE, le `SolverManager` continue son exécution sans erreur ni interruption.

---

### Story 1.3: Consultation de Statut Unitaire & Explication du Score (`GET /planning/jobs/{jobId}`)

As an API client / frontend user,  
I want to query the current status and detailed score explanation of a specific job,  
So that I have a reliable polling fallback and insight into penalized constraints.

**Acceptance Criteria:**

**Given** un job existant (en cours ou terminé),  
**When** un client interroge `GET /planning/jobs/{jobId}`,  
**Then** le système retourne un code HTTP `200 OK` avec le statut courant (`SOLVING`, `COMPLETED`, `TERMINATED_EARLY`, ou `FAILED`), le score actuel et le détail des contraintes pénalisées via `SolutionManager.explain()`,  
**And** si le `jobId` n'existe pas dans le registre, le système retourne une réponse HTTP `404 Not Found`.

---

### Story 1.4: Interruption Manuelle du Solver & Sauvegarde Directe (`POST /planning/jobs/{jobId}/stop`)

As an administrator / scheduler,  
I want to stop a running solve job on demand and immediately persist the best solution found so far,  
So that I can halt execution when the score is satisfactory without waiting for the full timeout.

**Acceptance Criteria:**

**Given** un job dont le statut est `SOLVING`,  
**When** le client appelle `POST /planning/jobs/{jobId}/stop`,  
**Then** le système invoque `solverManager.terminateEarly(jobId)` et le calcul s'arrête sous 1 seconde,  
**And** la meilleure solution trouvée est immédiatement persistée via `plannifDataApiClient.savePlanning(solution)`,  
**And** le statut du job bascule à `TERMINATED_EARLY` avec l'ID persistant `savedPlanningId` renseigné,  
**And** l'arbre d'objets `Planning` en mémoire est libéré pour éviter l'encombrement du heap,  
**And** si le job est déjà terminé ou inconnu, un code HTTP approprié (`409 Conflict` ou `404 Not Found`) est retourné.

---

## Epic 2: Contrôle Pédagogique Interactif — Séances Verrouillées (Pinning) & Poids Dynamiques

Permettre aux planificateurs scolaires d'imposer des cours et salles fixes (séances verrouillées via `@PlanningPin`) et d'ajuster dynamiquement les priorités pédagogiques relatives (surcharges de poids de contraintes) afin de tester différents scénarios d'emploi du temps sans recompiler l'application.

### Story 2.1: Verrouillage Manuel de Séances Imposées (`@PlanningPin` sur `Seance`)

As a school scheduler,  
I want to mark specific course sessions as pinned,  
So that the solver does not alter their assigned start time, room, or teacher while optimizing other sessions.

**Acceptance Criteria:**

**Given** une entité `Seance` comportant le champ `boolean pinned` annoté `@PlanningPin`,  
**When** une séance est fournie avec `pinned = true`, une salle, un professeur et un créneau initialement définis,  
**Then** Timefold conserve strictement ces valeurs assignées durant toutes les étapes de recherche heuristique et locale (immutabilité garantie),  
**And** les séances non verrouillées sont réorganisées de manière à respecter les contraintes dures et souples en tenant compte des séances verrouillées,  
**And** le champ `pinned` est correctement sérialisé et mappé à travers les couches DTO et MapStruct (`SeanceDto`),  
**And** des tests unitaires (`SolverTest` ou `ConstraintVerifierTest`) valident l'immutabilité des séances épinglées.

---

### Story 2.2: Surcharge Dynamique des Poids de Contraintes via l'API

As a school administrator,  
I want to pass custom constraint weight overrides in the solving request payload,  
So that I can fine-tune educational trade-offs (e.g., student lunch break vs teacher max gap) without restarting or recompiling the application.

**Acceptance Criteria:**

**Given** une requête de soumission de job contenant une map `constraintWeightOverrides` (ex. `{"Teacher day off": "0hard/-5soft"}`),  
**When** le problème de planning est préparé avant soumission au `SolverManager`,  
**Then** les surcharges sont instanciées sous forme de `ConstraintWeightOverrides<HardSoftScore>` et injectées dans le fact `PlanningConstraintConfiguration` de la solution,  
**And** le calcul du score Timefold prend immédiatement en compte ces nouveaux coefficients pour pénaliser les solutions candidates,  
**And** un test unitaire automatisé démontre qu'une contrainte modifiée produit une pénalité multipliée conformément au poids injecté.
