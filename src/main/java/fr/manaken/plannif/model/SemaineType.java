package fr.manaken.plannif.model;

public enum SemaineType {
    SEMAINE_1,
    SEMAINE_2,
    SEMAINE_3;

    public static SemaineType fromIndex(int index) {
        return switch (index) {
            case 1 -> SEMAINE_1;
            case 2 -> SEMAINE_2;
            case 3 -> SEMAINE_3;
            default -> null;
        };
    }
}
