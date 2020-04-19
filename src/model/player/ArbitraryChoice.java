package model.player;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import model.abilities.abilitySlots.ChoiceList;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ArbitraryChoice implements ChoiceList<String> {
    private final List<String> choices;
    private final Consumer<String> fillFunction;
    private final Consumer<String> emptyFunction;
    private final String name;
    private final ObservableList<String> selections = FXCollections.observableArrayList();
    private int numSelections;

    ArbitraryChoice(String name, List<String> choices, Consumer<String> fillFunction, Consumer<String> emptyFunction, int numSelections) {
        this.name = name;
        this.choices = choices;
        this.fillFunction = fillFunction;
        this.emptyFunction = emptyFunction;
        this.numSelections = numSelections;
        if(choices instanceof ObservableList)
            ((ObservableList<String>) choices).addListener((ListChangeListener<String>) change->{
                while(change.next())
                    selections.removeAll(change.getRemoved());
            });
    }

    @Override
    public List<String> getOptions() {
        if(choices instanceof ObservableList)
            return FXCollections.unmodifiableObservableList((ObservableList<String>) choices);
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
    public ObservableList<String> getSelections() {
        return FXCollections.unmodifiableObservableList(selections);
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
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
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

    void increaseChoices(int amount) {
        numSelections += amount;
    }

    void decreaseChoices(int amount) {
        numSelections = Math.max(0, numSelections-amount);
        Iterator<String> iterator = selections.iterator();
        while(selections.size() > numSelections && iterator.hasNext()){
            iterator.next();
            iterator.remove();
        }
    }

    public void addAll(List<String> strings) {
        selections.addAll(strings);
    }
}
