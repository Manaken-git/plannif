# Plannif - Timefold Constraints

This project uses [Timefold Solver](https://timefold.ai/) to optimize the schedule. The following constraints are defined in the solver configuration to ensure a valid schedule.

## Hard Constraints
Hard constraints *must* be satisfied. If any hard constraint is broken, the schedule is considered infeasible.

### 1. Room Conflict
*   **Description**: A room cannot accommodate two different sessions at the same time.
*   **Implementation**: `roomConflict` in `PlanningConstraints.java`.
*   **Penalty**: 1 Hard point per conflict.

### 2. Teacher Conflict
*   **Description**: A professor cannot teach two different sessions at the same time.
*   **Implementation**: `teacherConflict` in `PlanningConstraints.java`.
*   **Penalty**: 1 Hard point per conflict.

### 3. Student Group Conflict (Class Conflict)
*   **Description**: A class (group of students) cannot attend two different sessions at the same time.
*   **Implementation**: `studentGroupConflict` in `PlanningConstraints.java`.
*   **Penalty**: 1 Hard point per conflict.

## Soft Constraints

### 1. Teacher Day Off
*   **Description**: A teacher can identify a specific day of the week (Monday=0 to Friday=4) as their day off.
*   **Implementation**: `teacherDayOff` in `PlanningConstraints.java` (linked via `t_professeur_dayoff` table).
*   **Penalty**: 1 Soft point per session assigned on a day off.


avoir une limite d'heure par jour, par semaine, par séance (1 contraintes par statement)
une limite d'heure par professeur, par classe => un professeur ne peut pas faire plus de 5 heures sur 2 jours d'affilés pour la même classe, quel que soit la salle et la matière

10h-12h 13h30-15h30 avoir des créneaux de préférence