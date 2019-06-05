package model.abilities.abilitySlots;

import javafx.beans.property.ReadOnlyObjectProperty;

public interface Choice<T> {
    void fill(T choice);
    T getChoice();
    void empty();
    ReadOnlyObjectProperty<T> getChoiceProperty();
}
