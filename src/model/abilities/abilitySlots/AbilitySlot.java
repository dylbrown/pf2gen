package model.abilities.abilitySlots;

import model.abilities.Ability;

public abstract class AbilitySlot {
    private final String name;

    Ability currentAbility;
    boolean preSet = false;
    private int level;

    AbilitySlot(String name, int level) {
        this.name = name;
        this.level = level;
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

    public int getLevel(){
        return level;
    }
}
