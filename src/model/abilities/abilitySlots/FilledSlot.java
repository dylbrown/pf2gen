package model.abilities.abilitySlots;

import model.abilities.Ability;

public class FilledSlot extends AbilitySlot {
    public FilledSlot(String name, int level, Ability currentAbility) {
        super(name, level);
        this.currentAbility = currentAbility;
        preSet = true;
    }
}
