package model.abilities.abilitySlots;

import model.abilities.Ability;

public class FilledSlot extends AbilitySlot {
    public FilledSlot(String name, Ability currentAbility) {
        super(name);
        this.currentAbility = currentAbility;
        preSet = true;
    }
}
