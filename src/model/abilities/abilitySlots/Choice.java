package model.abilities.abilitySlots;

import javafx.collections.ObservableList;

public interface Choice<T> {
    String getName();
    void add(T choice);
    void remove(T choice);
    void empty();
    int getNumSelections();
    ObservableList<T> getSelections();
    int getLevel();
}
