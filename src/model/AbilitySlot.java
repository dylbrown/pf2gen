package model;

import model.enums.AbilityType;

import java.util.ArrayList;
import java.util.List;

public class AbilitySlot {
    private final String name;
    List<AbilityType> allowedAbilityTypes = new ArrayList<>();
    private Ability currentAbility;
    private boolean preSet = false;

    public AbilitySlot(String name) {
        this.name = name;
    }

    public AbilitySlot(String name, List<AttributeMod> mods) {
        this(name);
        preSet = true;
        currentAbility = new Ability(name, mods);
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
}
