package model.abilities.abilitySlots;

import javafx.beans.property.ReadOnlyObjectWrapper;
import model.abilities.Ability;

public abstract class AbilitySlot {
    private final String name;

    ReadOnlyObjectWrapper<Ability> currentAbility;
    boolean preSet = false;
    private final int level;

    AbilitySlot(String name, int level) {
        this.name = name;
        this.level = level;
        currentAbility = new ReadOnlyObjectWrapper<>(null);
    }

    public String getName() {
        return name;
    }

    public boolean isPreSet() {
        return preSet;
    }

    public Ability getCurrentAbility() {
        if(currentAbility == null) return null;
        return currentAbility.get();
    }

    @Override
    public String toString() {
        return name;
    }

    public int getLevel(){
        return level;
    }
}
