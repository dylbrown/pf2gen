package model.equipment;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.collections.ObservableList;
import model.ability_slots.Choice;

public class UnmodifiableChoice<T> implements Choice<T> {

    private final Choice<T> choice;

    public UnmodifiableChoice(Choice<T> choice) {
        this.choice = choice;
    }
    @Override
    public Class<T> getOptionsClass() {
        return choice.getOptionsClass();
    }

    @Override
    public String getName() {
        return choice.getName();
    }

    @Override
    public void add(T choice) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(T choice) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void empty() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMaxSelections() {
        return choice.getMaxSelections();
    }

    @Override
    public ReadOnlyIntegerProperty numSelectionsProperty() {
        return choice.numSelectionsProperty();
    }

    @Override
    public ReadOnlyIntegerProperty maxSelectionsProperty() {
        return choice.maxSelectionsProperty();
    }

    @Override
    public ObservableList<T> getSelections() {
        return choice.getSelections();
    }

    @Override
    public int getLevel() {
        return choice.getLevel();
    }

    @Override
    public UnmodifiableChoice<T> copy() {
        return this;
    }

    public Choice<T> copyBaseChoice() {
        return choice.copy();
    }
}
