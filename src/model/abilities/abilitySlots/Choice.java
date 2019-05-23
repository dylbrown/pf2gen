package model.abilities.abilitySlots;

import java.util.List;

public interface Choice<T> {
    List<T> getOptions();
    void fill(T choice);
    T getChoice();
    void empty();
}
