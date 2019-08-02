package model.abilities.abilitySlots;

import model.abilities.Ability;
import model.data_managers.FeatsManager;

public class DynamicFilledSlot extends AbilitySlot {
    private String contents;
    public DynamicFilledSlot(String name, int level, String contents) {
        super(name, level);
        preSet = true;
        this.contents = contents;
    }

    @Override
    public Ability getCurrentAbility() {
        return FeatsManager.find(contents);
    }
}
