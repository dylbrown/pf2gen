package model.abilities.abilitySlots;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;

public interface SingleChoice<T> extends Choice<T> {
    void fill(T choice);
    @Override
    default void add(T choice) {
        fill(choice);
    }
    @Override
    default void remove(T choice) {
        fill(null);
    }
    @Override
    default int getNumSelections() {return 1;}
    T getChoice();

    @Override
    default ObservableList<T> getSelections(){
        throw new UnsupportedOperationException();
    }

    ReadOnlyObjectProperty<T> getChoiceProperty();
}
