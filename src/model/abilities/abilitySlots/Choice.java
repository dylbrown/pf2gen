package model.abilities.abilitySlots;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.collections.ObservableList;

public interface Choice<T> {
    Class<T> getOptionsClass();
    String getName();
    void add(T choice);
    void remove(T choice);
    void empty();
    int getNumSelections();
    ReadOnlyIntegerProperty numSelectionsProperty();
    ObservableList<T> getSelections();
    int getLevel();
}
