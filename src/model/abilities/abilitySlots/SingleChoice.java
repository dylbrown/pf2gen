package model.abilities.abilitySlots;

import javafx.beans.property.ReadOnlyObjectProperty;

import java.util.Collections;
import java.util.List;

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
    default List<T> getSelections(){
        if(getChoice() == null)
            return Collections.emptyList();
        else
            return Collections.singletonList(getChoice());
    }

    ReadOnlyObjectProperty<T> getChoiceProperty();
}
