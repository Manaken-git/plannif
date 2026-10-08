package fr.manaken.plannif.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

public class TeacherClassWork {
    private final Professeur professeur;
    private final Classe classe;
    private final LocalDate date;
    private final long minutes;

    public TeacherClassWork(Professeur professeur, Classe classe, LocalDate date, long minutes) {
        this.professeur = professeur;
        this.classe = classe;
        this.date = date;
        this.minutes = minutes;
    }

    public TeacherClassWork(Professeur professeur, Classe classe, LocalDate date, BigDecimal hours) {
        this.professeur = professeur;
        this.classe = classe;
        this.date = date;
        this.minutes = hours != null ? hours.multiply(BigDecimal.valueOf(60)).longValue() : 0L;
    }

    public Professeur getProfesseur() {
        return professeur;
    }

    public Classe getClasse() {
        return classe;
    }

    public LocalDate getDate() {
        return date;
    }

    public long getMinutes() {
        return minutes;
    }

    public BigDecimal getHours() {
        return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TeacherClassWork that = (TeacherClassWork) o;
        return minutes == that.minutes
                && Objects.equals(professeur, that.professeur)
                && Objects.equals(classe, that.classe)
                && Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(professeur, classe, date, minutes);
    }
}
