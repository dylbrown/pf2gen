package model.ability_slots;

import java.util.Collections;
import java.util.List;

public class DynamicChoiceSlot extends FeatSlot {
    public DynamicChoiceSlot(String abilityName, int level, String choiceType) {
        this(abilityName, level, Collections.singletonList("choice(" + choiceType+ ")"));
    }

    private DynamicChoiceSlot(String abilityName, int level, List<String> allowedTypes) {
        super(abilityName, level, allowedTypes);
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public DynamicChoiceSlot copy() {
        return new DynamicChoiceSlot(getName(), getLevel(), getAllowedTypes());
    }
}
