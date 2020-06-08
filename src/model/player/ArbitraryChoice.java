package model.player;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
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
    private final ReadOnlyIntegerWrapper numSelections;
    private final boolean multipleSelect;

    ArbitraryChoice(String name, List<String> choices, int numSelections, boolean multipleSelect) {
        this(name ,choices, s->{}, s -> {}, numSelections, multipleSelect);
    }

    ArbitraryChoice(String name, List<String> choices, Consumer<String> fillFunction, Consumer<String> emptyFunction, int numSelections, boolean multipleSelect) {
        this.name = name;
        this.choices = choices;
        this.fillFunction = fillFunction;
        this.emptyFunction = emptyFunction;
        this.numSelections = new ReadOnlyIntegerWrapper(numSelections);
        this.multipleSelect = multipleSelect;
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
        if(this.selections.size() < numSelections.get() && this.choices.contains(choice) &&
                (this.multipleSelect || !this.selections.contains(choice))) {
            this.selections.add(choice);
            fillFunction.accept(choice);
        }
    }

    @Override
    public void remove(String choice) {
        boolean remove = this.selections.remove(choice);
        if(remove) emptyFunction.accept(choice);
    }

    @Override
    public int getNumSelections() {
        return numSelections.get();
    }

    @Override
    public ReadOnlyIntegerProperty numSelectionsProperty() {
        return numSelections.getReadOnlyProperty();
    }

    private final ObservableList<String> unmodifiableSelections = FXCollections.unmodifiableObservableList(selections);
    @Override
    public ObservableList<String> getSelections() {
        return unmodifiableSelections;
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
        if(amount < 0) decreaseChoices(-1 * amount);
        else numSelections.set(numSelections.get() + amount);
    }

    void decreaseChoices(int amount) {
        numSelections.set(Math.max(0, numSelections.get()-amount));
        Iterator<String> iterator = selections.iterator();
        while(selections.size() > numSelections.get() && iterator.hasNext()){
            iterator.next();
            iterator.remove();
        }
    }

    public void addAll(List<String> strings) {
        for (String string : strings) {
            add(string);
        }

    }

    public void clear() {
        while(selections.size() > 0) {
            remove(selections.get(0));
        }
    }
}
