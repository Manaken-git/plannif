package fr.manaken.plannif.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class TeacherClassWork {
    private final Professeur professeur;
    private final Classe classe;
    private final LocalDate date;
    private final BigDecimal hours;

    public TeacherClassWork(Professeur professeur, Classe classe, LocalDate date, BigDecimal hours) {
        this.professeur = professeur;
        this.classe = classe;
        this.date = date;
        this.hours = hours;
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

    public BigDecimal getHours() {
        return hours;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TeacherClassWork that = (TeacherClassWork) o;
        return Objects.equals(professeur, that.professeur) && Objects.equals(classe, that.classe)
                && Objects.equals(date, that.date)
                && (hours == null ? that.hours == null : (that.hours != null && hours.compareTo(that.hours) == 0));
    }

    @Override
    public int hashCode() {
        return Objects.hash(professeur, classe, date, hours == null ? 0 : hours.stripTrailingZeros());
    }
}
