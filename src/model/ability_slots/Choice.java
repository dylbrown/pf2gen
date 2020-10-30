package model.ability_slots;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.collections.ObservableList;

public interface Choice<T> {
    Class<T> getOptionsClass();
    String getName();
    void add(T choice);
    void remove(T choice);
    void empty();
    int getMaxSelections();
    ReadOnlyIntegerProperty numSelectionsProperty();
    ReadOnlyIntegerProperty maxSelectionsProperty();
    ObservableList<T> getSelections();
    int getLevel();
    Choice<T> copy();
}
