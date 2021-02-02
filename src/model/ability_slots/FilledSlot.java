package model.ability_slots;

import model.abilities.Ability;

public class FilledSlot extends AbilitySlot {
    public FilledSlot(String name, int level, Ability currentAbility) {
        super(name, level);
        this.currentAbility.set(currentAbility);
        preSet = true;
    }

    @Override
    public AbilitySlot copy() {
        return this;
    }
}
