package model.abilities.abilitySlots;

import java.util.List;

public interface ChoiceList<T> extends Choice<T> {
    List<T> getOptions();
}
