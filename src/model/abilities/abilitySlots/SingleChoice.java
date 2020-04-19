package model.abilities.abilitySlots;

import javafx.beans.property.ReadOnlyObjectProperty;

public interface SingleChoice<T> extends Choice<T> {
    @Override
    default int getNumSelections() {return 1;}
    T getChoice();
    void fill(T choice);

    ReadOnlyObjectProperty<T> getChoiceProperty();
}
