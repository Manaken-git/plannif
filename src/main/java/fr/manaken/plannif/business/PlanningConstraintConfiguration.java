package fr.manaken.plannif.business;

import ai.timefold.solver.core.api.domain.constraintweight.ConstraintConfiguration;
import ai.timefold.solver.core.api.domain.constraintweight.ConstraintWeight;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConstraintConfiguration(constraintPackage = "fr.manaken.plannif.business")
public class PlanningConstraintConfiguration {

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
    public static final String ALIGNED_SESSIONS = "Aligned sessions synchronization";

    // Phase 5: Enseignants Avancés & Déplacements
    public static final String TEACHER_LUNCH_BREAK = "Teacher lunch break";
    public static final String TEACHER_UNAVAILABILITY = "Teacher unavailability";
    public static final String TEACHER_TRAVEL_DISTANCE = "Teacher travel distance between buildings";

    // Phase 6: Pédagogique
    public static final String HIGH_COGNITIVE_LOAD = "High cognitive load morning preference";
    public static final String SESSION_PAIRING = "Session pairing 2h block preference";

    // Poids des contraintes
    @ConstraintWeight(ROOM_CONFLICT)
    private HardSoftScore roomConflict = HardSoftScore.ONE_HARD;

    @ConstraintWeight(TEACHER_CONFLICT)
    private HardSoftScore teacherConflict = HardSoftScore.ONE_HARD;

    @ConstraintWeight(STUDENT_GROUP_CONFLICT)
    private HardSoftScore studentGroupConflict = HardSoftScore.ONE_HARD;

    @ConstraintWeight(STUDENT_GROUP_PRESENCE)
    private HardSoftScore studentGroupPresence = HardSoftScore.ONE_HARD;

    @ConstraintWeight(TEACHER_MUST_BE_QUALIFIED)
    private HardSoftScore teacherMustBeQualified = HardSoftScore.ONE_HARD;

    @ConstraintWeight(SUBJECT_CLASS_PERIOD)
    private HardSoftScore subjectClassPeriod = HardSoftScore.ONE_HARD;

    @ConstraintWeight(HOLIDAY_CONFLICT)
    private HardSoftScore holidayConflict = HardSoftScore.ONE_HARD;

    @ConstraintWeight(VIE_DE_CLASSE_TIMING)
    private HardSoftScore vieDeClasseTiming = HardSoftScore.ONE_HARD;

    @ConstraintWeight(SEANCE_TYPE_DURATION_MATCH)
    private HardSoftScore seanceTypeDurationMatch = HardSoftScore.ONE_HARD;

    @ConstraintWeight(TEACHER_DAY_OFF)
    private HardSoftScore teacherDayOff = HardSoftScore.ONE_SOFT;

    @ConstraintWeight(TEACHER_MAX_HOURS_PER_DAY)
    private HardSoftScore teacherMaxHoursPerDay = HardSoftScore.ONE_SOFT;

    @ConstraintWeight(TEACHER_MAX_HOURS_PER_WEEK)
    private HardSoftScore teacherMaxHoursPerWeek = HardSoftScore.ONE_SOFT;

    @ConstraintWeight(TEACHER_MAX_HOURS_PER_SESSION)
    private HardSoftScore teacherMaxHoursPerSession = HardSoftScore.ONE_SOFT;

    @ConstraintWeight(TEACHER_CLASS_MAX_HOURS_OVER_TWO_DAYS)
    private HardSoftScore teacherClassMaxHoursOverTwoDays = HardSoftScore.ONE_SOFT;

    @ConstraintWeight(TEACHER_MAX_GAP)
    private HardSoftScore teacherMaxGap = HardSoftScore.ONE_SOFT;

    @ConstraintWeight(SUBJECT_CLASS_MAX_SESSIONS_PER_DAY)
    private HardSoftScore subjectClassMaxSessionsPerDay = HardSoftScore.ONE_SOFT;

    @ConstraintWeight(SUBJECT_CLASS_SPREADING)
    private HardSoftScore subjectClassSpreading = HardSoftScore.ONE_SOFT;

    @ConstraintWeight(ROOM_CAPACITY)
    private HardSoftScore roomCapacity = HardSoftScore.ONE_HARD;

    @ConstraintWeight(ROOM_EQUIPMENT_MATCH)
    private HardSoftScore roomEquipmentMatch = HardSoftScore.ONE_HARD;

    @ConstraintWeight(STUDENT_LUNCH_BREAK)
    private HardSoftScore studentLunchBreak = HardSoftScore.ONE_SOFT;

    @ConstraintWeight(STUDENT_MAX_HOURS_PER_DAY)
    private HardSoftScore studentMaxHoursPerDay = HardSoftScore.ONE_SOFT;

    @ConstraintWeight(STUDENT_MAX_GAP)
    private HardSoftScore studentMaxGap = HardSoftScore.ONE_SOFT;

    @ConstraintWeight(ALIGNED_SESSIONS)
    private HardSoftScore alignedSessions = HardSoftScore.ONE_HARD;

    @ConstraintWeight(TEACHER_LUNCH_BREAK)
    private HardSoftScore teacherLunchBreak = HardSoftScore.ONE_SOFT;

    @ConstraintWeight(TEACHER_UNAVAILABILITY)
    private HardSoftScore teacherUnavailability = HardSoftScore.ONE_HARD;

    @ConstraintWeight(TEACHER_TRAVEL_DISTANCE)
    private HardSoftScore teacherTravelDistance = HardSoftScore.ONE_SOFT;

    @ConstraintWeight(HIGH_COGNITIVE_LOAD)
    private HardSoftScore highCognitiveLoad = HardSoftScore.ONE_SOFT;

    @ConstraintWeight(SESSION_PAIRING)
    private HardSoftScore sessionPairing = HardSoftScore.ONE_SOFT;
}
