package model.ability_slots;

import java.util.List;

public interface ChoiceList<T> extends Choice<T> {
    List<T> getOptions();
}
