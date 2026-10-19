---
id: SPEC-plannif-async-and-pinning
companions:
  - api-contract.md
  - ../../planning-artifacts/architecture/architecture-plannif-2026-10-18/ARCHITECTURE-SPINE.md
sources: []
---

> **Canonical contract.** This SPEC and the files in `companions:` are the complete, preservation-validated contract for what to build, test, and validate. Source documents listed in frontmatter are for traceability — consult them only if you need narrative rationale or prose color this contract intentionally omits.

# Plannif — Résolution Asynchrone, Pinning & Pilotage Dynamique

## Why

Le moteur d'optimisation Timefold actuel résout 28 contraintes métier complexes pour les lycées de manière synchrone et bloquante (jusqu'à 120 secondes ou plus), ce qui bloque les connexions HTTP et prive l'utilisateur de toute visibilité sur la progression du calcul. De plus, les planificateurs scolaires ne disposent d'aucun moyen pour verrouiller des créneaux ou salles imposés (*pinning*), ni pour ajuster les arbitrages pédagogiques (poids des contraintes) à la volée depuis l'interface sans recompiler l'application. Cette spécification apporte le contrôle asynchrone par streaming Server-Sent Events (SSE), la souplesse d'ajustement dynamique et la cohabitation entre décisions humaines imposées et optimisation automatique.

## Capabilities

- **CAP-1**
  - **intent:** Le client peut initier une résolution sans bloquer l'appel HTTP et recevoir un identifiant unique de tâche (`jobId`).
  - **success:** `POST /planning/jobs` retourne un code HTTP `202 Accepted` avec un `jobId` ; le traitement s'exécute en arrière-plan via `SolverManager` et libère immédiatement le thread appelant.

- **CAP-2**
  - **intent:** Le client peut s'abonner au flux d'événements de progression en temps réel pour observer les améliorations successives du score et recevoir la notification de fin avec l'ID du planning persisté.
  - **success:** `GET /planning/jobs/{jobId}/progress` ouvre un flux Server-Sent Events (`text/event-stream`) émettant chaque nouveau meilleur score intermédiaire (`score-update`) et un événement terminal (`completed` ou `terminated`) contenant le `savedPlanningId`.

- **CAP-3**
  - **intent:** L'utilisateur peut interrompre un calcul en cours et persister immédiatement la meilleure solution trouvée jusqu'alors.
  - **success:** `POST /planning/jobs/{jobId}/stop` arrête le `SolverJob` correspondant sous 1 seconde, persiste la meilleure solution dans `planning-data`, libère l'objet lourd en mémoire et bascule l'état en `TERMINATED_EARLY`.

- **CAP-4**
  - **intent:** L'utilisateur peut figer (verrouiller) la date, la salle et/ou le professeur sur des séances spécifiques pour que le solveur ne modifie pas ces assignations.
  - **success:** Une séance marquée avec `@PlanningPin boolean isPinned()` conserve strictement ses valeurs initiales de variables de planification durant toute la phase de résolution.

- **CAP-5**
  - **intent:** L'utilisateur peut surcharger dynamiquement le poids et le type de score des contraintes pour une résolution donnée.
  - **success:** Un appel transmettant des poids personnalisés dans `ConstraintWeightOverrides` altère mesurablement le comportement du solveur et le score final sans modification des annotations du code.

## Constraints

- Utilisation obligatoire du `SolverManager<Planning, String>` natif de Timefold 1.27.0 sans dépendance à un courtier de messages externe (RabbitMQ/Kafka).
- Non-régression absolue : les 53 tests unitaires et d'intégration existants doivent continuer de passer avec succès.
- Les calculs de durées et intervalles doivent continuer de s'effectuer en `long` (minutes) pour préserver la vitesse d'évaluation du solveur.
- La persistance vers `planning-data` est déclenchée immédiatement dès l'arrêt ou la complétion du job, permettant de purger l'objet mémoire lourd pour éviter toute fuite mémoire.

## Non-goals

- Refonte complète de l'interface Vue.js (la spécification définit le contrat d'API exposé au frontend).
- Clustering distribué multi-nœuds du solveur (un seul nœud Spring Boot gère la file de résolutions).
- Cache mémoire permanent des solutions terminées (la base de données relationnelle via `planning-data` constitue l'unique source de vérité pour le rechargement).

## Success signal

Un utilisateur déclenche un calcul d'emploi du temps complexe via `POST /planning/jobs`, visualise en direct la convergence du score via le flux SSE `GET /planning/jobs/{jobId}/progress`, interrompt le solveur si nécessaire via `POST /planning/jobs/{jobId}/stop`, et constate que la solution finale est immédiatement persistée dans `planning-data` avec respect absolu des séances verrouillées (`pinned: true`).

## Assumptions

- Le nombre de calculs lourds concurrents sur une même instance ne dépasse pas 3 à 5 résolutions simultanées (géré par le pool de workers Timefold).
- Le frontend utilise l'API standard `EventSource` du navigateur pour écouter le flux SSE.
