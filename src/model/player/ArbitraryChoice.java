package model.player;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import model.ability_slots.ChoiceList;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ArbitraryChoice<T> implements ChoiceList<T> {
    private final ObservableList<T> choices;
    private final Consumer<T> fillFunction;
    private final Consumer<T> emptyFunction;
    private final String name;
    private final ObservableList<T> selections = FXCollections.observableArrayList();
    private final ReadOnlyIntegerWrapper numSelections, maxSelections;
    private final boolean multipleSelect;
    private final Class<T> optionsClass;

    private ArbitraryChoice(Builder<T> builder) {
        this.name = builder.name;
        this.choices = builder.choices;
        this.fillFunction = builder.fillFunction;
        this.emptyFunction = builder.emptyFunction;
        this.maxSelections = new ReadOnlyIntegerWrapper(builder.maxSelections);
        this.numSelections = new ReadOnlyIntegerWrapper(0);
        this.selections.addListener((ListChangeListener<T>) c-> numSelections.set(this.selections.size()));
        this.multipleSelect = builder.multipleSelect;
        this.optionsClass = builder.optionsClass;
        choices.addListener((ListChangeListener<T>) change->{
            while(change.next()) {
                for (T t : change.getRemoved()) {
                    remove(t);
                }
            }
        });
    }

    @Override
    public ObservableList<T> getOptions() {
        return FXCollections.unmodifiableObservableList(choices);
    }

    @Override
    public void add(T choice) {
        if(this.selections.size() < maxSelections.get() && this.choices.contains(choice) &&
                (this.multipleSelect || !this.selections.contains(choice))) {
            this.selections.add(choice);
            if(fillFunction != null)
                fillFunction.accept(choice);
        }
    }

    @Override
    public void remove(T choice) {
        boolean remove = this.selections.remove(choice);
        if(remove && emptyFunction != null) emptyFunction.accept(choice);
    }

    @Override
    public int getMaxSelections() {
        return maxSelections.get();
    }

    @Override
    public ReadOnlyIntegerProperty numSelectionsProperty() {
        return numSelections.getReadOnlyProperty();
    }

    @Override
    public ReadOnlyIntegerProperty maxSelectionsProperty() {
        return maxSelections.getReadOnlyProperty();
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
    public ArbitraryChoice<T> copy() {
        return new Builder<>(this).build();
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
        else maxSelections.set(maxSelections.get() + amount);
    }

    void decreaseChoices(int amount) {
        maxSelections.set(Math.max(0, maxSelections.get()-amount));
        Iterator<T> iterator = selections.iterator();
        while(selections.size() > maxSelections.get() && iterator.hasNext()){
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

    public static class Builder<T> {
        private ObservableList<T> choices;
        private Consumer<T> fillFunction;
        private Consumer<T> emptyFunction;
        private String name;
        private int maxSelections;
        private boolean multipleSelect = false;
        private Class<T> optionsClass;

        public Builder(){}

        public Builder(ArbitraryChoice<T> choice) {
            choices = choice.choices;
            fillFunction = choice.fillFunction;
            emptyFunction = choice.emptyFunction;
            name = choice.name;
            maxSelections = choice.maxSelections.get();
            multipleSelect = choice.multipleSelect;
            optionsClass = choice.optionsClass;
        }

        public void setChoicesConstant(List<T> list) {
            setChoices(FXCollections.observableList(list));
        }

        public void setChoices(ObservableList<T> choices) {
            this.choices = choices;
        }

        public void setFillFunction(Consumer<T> fillFunction) {
            this.fillFunction = fillFunction;
        }

        public void setEmptyFunction(Consumer<T> emptyFunction) {
            this.emptyFunction = emptyFunction;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setMaxSelections(int maxSelections) {
            this.maxSelections = maxSelections;
        }

        public void setMultipleSelect(boolean multipleSelect) {
            this.multipleSelect = multipleSelect;
        }

        public void setOptionsClass(Class<T> optionsClass) {
            this.optionsClass = optionsClass;
        }

        public ArbitraryChoice<T> build() {
            return new ArbitraryChoice<>(this);
        }
    }
}
