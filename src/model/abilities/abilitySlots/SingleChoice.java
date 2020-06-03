package model.abilities.abilitySlots;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;

public interface SingleChoice<T> extends Choice<T> {
    @Override
    default int getNumSelections() {return 1;}
    @Override
    default ReadOnlyIntegerProperty numSelectionsProperty() {
        return new SimpleIntegerProperty(1);
    }
    T getChoice();
    void fill(T choice);

    ReadOnlyObjectProperty<T> getChoiceProperty();
}
