package model.abilities.abilitySlots;

import java.util.List;

public interface Choice<T> {
    void add(T choice);
    void remove(T choice);
    void empty();
    int getNumSelections();
    List<T> getSelections();
    int getLevel();
}
