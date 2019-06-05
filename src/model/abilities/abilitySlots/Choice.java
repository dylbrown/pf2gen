package model.abilities.abilitySlots;

import javafx.beans.property.ReadOnlyObjectProperty;

import java.util.List;

public interface Choice<T> {
    List<T> getOptions();
    void fill(T choice);
    T getChoice();
    void empty();
    ReadOnlyObjectProperty<T> getChoiceProperty();
}
