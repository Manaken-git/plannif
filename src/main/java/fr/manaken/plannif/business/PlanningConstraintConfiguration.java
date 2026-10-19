package fr.manaken.plannif.business;

public final class PlanningConstraintConfiguration {

    private PlanningConstraintConfiguration() {
        // Utility class
    }

    // Contraintes existantes / Refactorisées
    public static final String ROOM_CONFLICT = "Room conflict";
    public static final String TEACHER_CONFLICT = "Teacher conflict";
    public static final String STUDENT_GROUP_CONFLICT = "Student group conflict";
    public static final String STUDENT_GROUP_PRESENCE = "Student group presence";
    public static final String TEACHER_MUST_BE_QUALIFIED = "Teacher must be qualified for the subject";
    public static final String SUBJECT_CLASS_PERIOD = "Subject class period";
    public static final String HOLIDAY_CONFLICT = "Holiday conflict";
    public static final String VIE_DE_CLASSE_TIMING = "Vie de classe timing constraint";
    public static final String SEANCE_TYPE_DURATION_MATCH = "Seance type and slot duration match";

    public static final String TEACHER_DAY_OFF = "Teacher day off";
    public static final String TEACHER_MAX_HOURS_PER_DAY = "Max hours per day for teacher";
    public static final String TEACHER_MAX_HOURS_PER_WEEK = "Max hours per week for teacher";
    public static final String TEACHER_MAX_HOURS_PER_SESSION = "Max hours per session for teacher";
    public static final String TEACHER_CLASS_MAX_HOURS_OVER_TWO_DAYS = "Teacher class max 5h consecutive 2 days";
    public static final String TEACHER_MAX_GAP = "Teacher max gap 2h";
    public static final String SUBJECT_CLASS_MAX_SESSIONS_PER_DAY = "Subject class max sessions per day";
    public static final String SUBJECT_CLASS_SPREADING = "Subject class spreading penalty";

    // Phase 3: Salles & Équipements
    public static final String ROOM_CAPACITY = "Room capacity";
    public static final String ROOM_EQUIPMENT_MATCH = "Room equipment and type match";

    // Phase 4: Rythme Élèves
    public static final String STUDENT_LUNCH_BREAK = "Student lunch break";
    public static final String STUDENT_MAX_HOURS_PER_DAY = "Student max hours per day";
    public static final String STUDENT_MAX_GAP = "Student max gap";
    public static final String STUDENT_DAY_START_TIME = "Student day start time";
    public static final String ALIGNED_SESSIONS = "Aligned sessions synchronization";

    // Phase 5: Enseignants Avancés & Déplacements
    public static final String TEACHER_LUNCH_BREAK = "Teacher lunch break";
    public static final String TEACHER_UNAVAILABILITY = "Teacher unavailability";
    public static final String TEACHER_TRAVEL_DISTANCE = "Teacher travel distance between buildings";

    // Phase 6: Pédagogique
    public static final String HIGH_COGNITIVE_LOAD = "High cognitive load morning preference";
    public static final String SESSION_PAIRING = "Session pairing 2h block preference";
}
