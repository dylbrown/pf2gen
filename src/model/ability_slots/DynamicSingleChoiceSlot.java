package model.ability_slots;

import model.abilities.Ability;

import java.util.List;
import java.util.function.Supplier;

public class DynamicSingleChoiceSlot extends SingleChoiceSlot {
    private final Supplier<List<Ability>> getOptions;

    public DynamicSingleChoiceSlot(String abilityName, int level, Supplier<List<Ability>> getOptions) {
        super(abilityName, level);
        this.getOptions = getOptions;
    }

    @Override
    public List<Ability> getOptions() {
        return getOptions.get();
    }
}
