package model.abilities;

import model.AttributeMod;
import model.abilities.abilitySlots.AbilitySlot;
import model.ability_scores.AbilityMod;

import java.util.Collections;
import java.util.List;

public class Ability {
    private final List<String> prerequisites;
    private final List<AttributeMod> requiredAttrs;
    private final String customMod;
    private final List<AbilitySlot> abilitySlots;
    List<AttributeMod> modifiers;
    private List<AbilityMod> abilityMods;
    private final String name;
    private final String description;
    private final int level;

    private Ability(String name, int level, String description, List<String> prerequisites, List<AttributeMod> requiredAttrs, String customMod, List<AbilitySlot> abilitySlots) {
        this.name = name;
        this.description = description;
        this.prerequisites = prerequisites;
        this.level = level;
        if(requiredAttrs.size() == 0)
            this.requiredAttrs = Collections.emptyList();
        else
            this.requiredAttrs = requiredAttrs;
        abilityMods = Collections.emptyList();
        this.customMod = customMod;
        this.abilitySlots = abilitySlots;
    }

    Ability(int level, String name, String description, List<String> prerequisites, List<AttributeMod> requiredAttrs, String customMod, List<AbilitySlot> abilitySlots) {
        this(name, level, description, prerequisites, requiredAttrs, customMod, abilitySlots);
        this.modifiers= Collections.emptyList();
    }

    public Ability(int level, String name, List<AttributeMod> mods, String description, List<String> prerequisites, List<AttributeMod> requiredAttrs, String customMod, List<AbilitySlot> abilitySlots) {
        this(name, level, description, prerequisites, requiredAttrs, customMod, abilitySlots);
        this.modifiers = mods;
    }

    public Ability(int level, String name, List<AbilityMod> boosts, String description, List<AttributeMod> requiredAttrs, String customMod, List<AbilitySlot> abilitySlots) {
        this(level, name, description, Collections.emptyList(), requiredAttrs, customMod, abilitySlots);
        this.abilityMods = boosts;
    }

    public List<AttributeMod> getModifiers() {
        return Collections.unmodifiableList(modifiers);
    }

    public List<AbilityMod> getAbilityMods() {
        return Collections.unmodifiableList(abilityMods);
    }

    public String getDesc() {
        return description;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getName() {return name;}

    public int getLevel() {
        return level;
    }

    public List<AttributeMod> getRequiredAttrs() {
        return Collections.unmodifiableList(requiredAttrs);
    }

    public List<String> getPrerequisites() {
        return Collections.unmodifiableList(prerequisites);
    }

    public String getCustomMod() {
        return customMod;
    }

    public List<AbilitySlot> getAbilitySlots() {
        return Collections.unmodifiableList(abilitySlots);
    }
}
