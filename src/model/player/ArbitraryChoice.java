package model.player;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import model.ability_slots.ChoiceList;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ArbitraryChoice<T> implements ChoiceList<T> {
    private final List<T> choices;
    private final Consumer<T> fillFunction;
    private final Consumer<T> emptyFunction;
    private final String name;
    private final ObservableList<T> selections = FXCollections.observableArrayList();
    private final ReadOnlyIntegerWrapper numSelections;
    private final boolean multipleSelect;
    private final Class<T> optionsClass;

    ArbitraryChoice(String name, List<T> choices, int numSelections, boolean multipleSelect, Class<T> optionsClass) {
        this(name ,choices, s->{}, s -> {}, numSelections, multipleSelect, optionsClass);
    }

    ArbitraryChoice(String name, List<T> choices, Consumer<T> fillFunction, Consumer<T> emptyFunction, int numSelections, boolean multipleSelect, Class<T> optionsClass) {
        this.name = name;
        this.choices = choices;
        this.fillFunction = fillFunction;
        this.emptyFunction = emptyFunction;
        this.numSelections = new ReadOnlyIntegerWrapper(numSelections);
        this.multipleSelect = multipleSelect;
        this.optionsClass = optionsClass;
        if(choices instanceof ObservableList)
            ((ObservableList<T>) choices).addListener((ListChangeListener<T>) change->{
                while(change.next()) {
                    for (T t : change.getRemoved()) {
                        remove(t);
                    }
                }
            });
    }

    @Override
    public List<T> getOptions() {
        if(choices instanceof ObservableList)
            return FXCollections.unmodifiableObservableList((ObservableList<T>) choices);
        return Collections.unmodifiableList(choices);
    }

    @Override
    public void add(T choice) {
        if(this.selections.size() < numSelections.get() && this.choices.contains(choice) &&
                (this.multipleSelect || !this.selections.contains(choice))) {
            this.selections.add(choice);
            fillFunction.accept(choice);
        }
    }

    @Override
    public void remove(T choice) {
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

    private final ObservableList<T> unmodifiableSelections = FXCollections.unmodifiableObservableList(selections);
    @Override
    public ObservableList<T> getSelections() {
        return unmodifiableSelections;
    }

    @Override
    public int getLevel() {
        return 0;
    }

    @Override
    public void empty() {
        Iterator<T> iterator = selections.iterator();
        while(iterator.hasNext()) {
            T selection = iterator.next();
            iterator.remove();
            emptyFunction.accept(selection);
        }
    }

    @Override
    public Class<T> getOptionsClass() {
        return optionsClass;
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
        ArbitraryChoice<?> that = (ArbitraryChoice<?>) o;
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
        Iterator<T> iterator = selections.iterator();
        while(selections.size() > numSelections.get() && iterator.hasNext()){
            iterator.next();
            iterator.remove();
        }
    }

    public void addAll(List<T> items) {
        for (T item : items) {
            add(item);
        }

    }

    public void clear() {
        while(selections.size() > 0) {
            remove(selections.get(0));
        }
    }
}
