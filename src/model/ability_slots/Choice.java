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
    default boolean tryAdd(Object choice) {
        if(getOptionsClass().isInstance(choice)) {
            add(getOptionsClass().cast(choice));
            return true;
        }
        return false;
    }

    default boolean tryRemove(Object choice) {
        if(getOptionsClass().isInstance(choice)) {
            remove(getOptionsClass().cast(choice));
            return true;
        }
        return false;
    }
}
