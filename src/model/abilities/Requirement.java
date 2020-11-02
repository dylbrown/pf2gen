package model.abilities;

import model.enums.Proficiency;

import java.util.function.Function;

public interface Requirement<T> {
    static <T> Requirement<T> none() {
        return getProficiency -> true;
    }


    boolean test(Function<T, Proficiency> getProficiency);
}
