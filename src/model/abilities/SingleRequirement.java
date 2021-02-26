package model.abilities;

import model.enums.Proficiency;

import java.util.function.Function;

public class SingleRequirement<T> implements Requirement<T> {
    private final T t;
    private final Proficiency proficiency;

    public SingleRequirement(T t, Proficiency proficiency) {
        this.t = t;
        this.proficiency = proficiency;
    }

    public T get() {
        return t;
    }

    public Proficiency getProficiency() {
        return proficiency;
    }

    @Override
    public boolean test(Function<T, Proficiency> getProficiency) {
        return getProficiency.apply(t).getMod() >= proficiency.getMod();
    }
}
