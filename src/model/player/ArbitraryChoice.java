package model.player;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.abilities.abilitySlots.ChoiceList;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ArbitraryChoice implements ChoiceList<String> {
    private final List<String> choices;
    private final Consumer<String> fillFunction;
    private final Consumer<String> emptyFunction;
    private final String name;
    private final ObservableList<String> selections = FXCollections.observableArrayList();
    private final int numSelections;

    public ArbitraryChoice(String name, List<String> choices, Consumer<String> fillFunction, Consumer<String> emptyFunction, int numSelections) {
        this.name = name;
        this.choices = choices;
        this.fillFunction = fillFunction;
        this.emptyFunction = emptyFunction;
        this.numSelections = numSelections;
    }

    @Override
    public List<String> getOptions() {
        return Collections.unmodifiableList(choices);
    }

    @Override
    public void add(String choice) {
        this.selections.add(choice);
        fillFunction.accept(choice);
    }

    @Override
    public void remove(String choice) {
        this.selections.remove(choice);
        emptyFunction.accept(choice);
    }

    @Override
    public int getNumSelections() {
        return numSelections;
    }

    @Override
    public List<String> getSelections() {
        return Collections.unmodifiableList(selections);
    }

    @Override
    public int getLevel() {
        return 0;
    }

    @Override
    public void empty() {
        for (String selection : selections) {
            emptyFunction.accept(selection);
        }

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
