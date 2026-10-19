# Contrat d'API REST — Gestion Asynchrone, SSE & Pinning

Ce document d'accompagnement détaille les spécifications d'échanges d'API REST associées au noyau de spécification (`SPEC.md`).

---

## 1. Soumission d'une Résolution Asynchrone

* **Endpoint** : `POST /planning/jobs`
* **Headers** : `Content-Type: application/json`
* **Body** (optionnel) :
  ```json
  {
    "planningId": 12,
    "constraintWeightOverrides": {
      "Teacher max gap": "0hard/-2soft",
      "Student lunch break": "0hard/-3soft"
    }
  }
  ```
  *(Si le body est vide, le planning courant est chargé depuis `planning-data`)*
* **Réponse** : `202 Accepted`
  ```json
  {
    "jobId": "e81d4a7b-3b32-4d2c-88ab-d72b22bb88a1",
    "status": "SOLVING",
    "submittedAt": "2026-10-18T22:30:00",
    "solverSpentLimitSeconds": 120
  }
  ```

---

## 2. Streaming de Progression en Temps Réel (Server-Sent Events)

* **Endpoint** : `GET /planning/jobs/{jobId}/progress`
* **Headers** : `Accept: text/event-stream`
* **Comportement** :
  * Le backend ouvre un flux `SseEmitter` Spring Web.
  * À chaque amélioration du meilleur score par Timefold (`bestSolutionConsumer`), un événement `score-update` est émis.
  * À la fin du calcul, un événement terminal `completed` (ou `terminated`) est émis avec l'identifiant du planning sauvegardé dans `planning-data`.
* **Exemples d'événements SSE** :
  ```text
  event: score-update
  data: {"status":"SOLVING","elapsedSeconds":4,"score":"-1hard/-50soft","hardScore":-1,"softScore":-50}

  event: score-update
  data: {"status":"SOLVING","elapsedSeconds":12,"score":"0hard/-35soft","hardScore":0,"softScore":-35}

  event: completed
  data: {"status":"COMPLETED","finalScore":"0hard/-22soft","savedPlanningId":145,"durationSeconds":45}
  ```

---

## 3. Consultation Ponctuelle de l'État (Polling Fallback)

* **Endpoint** : `GET /planning/jobs/{jobId}`
* **Réponse** : `200 OK`
  ```json
  {
    "jobId": "e81d4a7b-3b32-4d2c-88ab-d72b22bb88a1",
    "status": "SOLVING", // "COMPLETED", "TERMINATED_EARLY", "FAILED"
    "elapsedSeconds": 15,
    "score": "0hard/-42soft",
    "savedPlanningId": null,
    "explanation": {
      "hardScore": 0,
      "softScore": -42,
      "summary": "...",
      "matches": [
        {
          "constraintName": "Teacher day off",
          "score": "-2soft",
          "count": 2
        }
      ]
    },
    "errorMessage": null
  }
  ```

---

## 4. Cycle de Vie & Persistance Sans Cache Lourd

* Dès que le solver atteint sa limite de temps ou est interrompu :
  1. La solution optimale est immédiatement enregistrée dans `planning-data` via `plannifDataApiClient.savePlanning(solution)`.
  2. Le `savedPlanningId` retourné par `planning-data` est associé au job.
  3. L'instance lourde `Planning` est libérée de la mémoire (Garbage Collector).
  4. Le client consulte ou recharge le planning résultant directement depuis `planning-data` via son `savedPlanningId`.

---

## 5. Interruption Manuelle du Solver

* **Endpoint** : `POST /planning/jobs/{jobId}/stop`
* **Réponse** : `200 OK`
  ```json
  {
    "jobId": "e81d4a7b-3b32-4d2c-88ab-d72b22bb88a1",
    "status": "TERMINATED_EARLY",
    "savedPlanningId": 146,
    "score": "0hard/-58soft",
    "message": "Solver job terminated manually. Best solution saved to planning-data."
  }
  ```

---

## 6. Modèle de Pinning sur `Seance`

Une séance peut comporter un attribut de verrouillage :
```json
{
  "id": 101,
  "classeId": 1,
  "professeurId": 4,
  "salleId": 12,
  "debut": "2026-10-19T09:00:00",
  "pinned": true
}
```
* **Règle Timefold** : Une méthode `@PlanningPin boolean isPinned()` sur l'entité `Seance` garantit que le solveur ne modifie aucune des variables de décision (`debut`, `salle`, `professeur`) de cette séance pendant la résolution.
