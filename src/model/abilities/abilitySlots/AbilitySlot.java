package model.abilities.abilitySlots;

import model.abilities.Ability;

public abstract class AbilitySlot {
    private final String name;

    protected Ability currentAbility;
    protected boolean preSet = false;
    private int level=1;

    protected AbilitySlot(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isPreSet() {
        return preSet;
    }

    public Ability getCurrentAbility() {
        return currentAbility;
    }

    @Override
    public String toString() {
        return name+" "+level;
    }
}
