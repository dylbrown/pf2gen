package model.player;

import model.abilities.abilitySlots.Choice;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ArbitraryChoice implements Choice<String> {
    private final List<String> choices;
    private final Consumer<String> fillFunction;
    private final String name;
    private String choice;

    public ArbitraryChoice(String name, List<String> choices, Consumer<String> fillFunction) {
        this.name = name;
        this.choices = choices;
        this.fillFunction = fillFunction;
    }
    @Override
    public List<String> getOptions() {
        return Collections.unmodifiableList(choices);
    }

    @Override
    public void fill(String choice) {
        this.choice = choice;
        fillFunction.accept(choice);
    }

    @Override
    public String getChoice() {
        return choice;
    }

    @Override
    public void empty() {
        if(choice != null)
            fillFunction.accept(choice);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArbitraryChoice that = (ArbitraryChoice) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
