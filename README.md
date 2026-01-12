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

### 4. Teacher Max Hours
*   **Description**: A professor has limits on working hours:
    *   **Max hours per day**: Defined by `max_heures_par_jour`.
    *   **Max hours per week**: Defined by `max_heures_par_semaine`.
    *   **Max duration per session**: Defined by `max_heures_par_seance`.
*   **Implementation**: `teacherMaxHoursPerDay`, `teacherMaxHoursPerWeek`, `teacherMaxHoursPerSession`.
*   **Penalty**: 1 Hard point per violation.

### 5. Teacher-Class Consecutive Days Limit
*   **Description**: A professor cannot teach more than **5 hours** for the **same class** over **2 consecutive days**.
*   **Implementation**: `teacherClassMaxHoursConsecutive`.
*   **Penalty**: 1 Hard point per violation.

## Soft Constraints

### 1. Teacher Day Off
*   **Description**: A teacher can identify a specific day of the week (Monday=0 to Friday=4) as their day off.
*   **Implementation**: `teacherDayOff` in `PlanningConstraints.java` (linked via `t_professeur_dayoff` table).
*   **Penalty**: 1 Soft point per session assigned on a day off.



10h-12h 13h30-15h30 avoir des créneaux de préférence
des plannings pour les classes