package model.abilities;

import model.AttributeMod;
import model.abilities.abilitySlots.AbilitySlot;
import model.ability_scores.AbilityMod;
import model.enums.Type;

import java.util.Collections;
import java.util.List;

public class Ability implements Comparable<Ability> {
    private final List<String> prerequisites;
    private final List<AttributeMod> requiredAttrs;
    private final String customMod;
    private final List<AbilitySlot> abilitySlots;
    private final Type type;
    List<AttributeMod> modifiers;
    private List<AbilityMod> abilityMods;
    private final String name;
    private final String description;
    private final int level;

    private Ability(String name, int level, String description, List<String> prerequisites, List<AttributeMod> requiredAttrs, String customMod, List<AbilitySlot> abilitySlots, Type type) {
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
        this.type = type;
    }

    Ability(int level, String name, String description, List<String> prerequisites, List<AttributeMod> requiredAttrs, String customMod, List<AbilitySlot> abilitySlots, Type type) {
        this(name, level, description, prerequisites, requiredAttrs, customMod, abilitySlots, type);
        this.modifiers= Collections.emptyList();
    }

    public Ability(int level, String name, List<AttributeMod> mods, String description, List<String> prerequisites, List<AttributeMod> requiredAttrs, String customMod, List<AbilitySlot> abilitySlots, Type type) {
        this(name, level, description, prerequisites, requiredAttrs, customMod, abilitySlots, type);
        this.modifiers = mods;
    }

    public Ability(int level, String name, List<AbilityMod> boosts, String description, List<AttributeMod> requiredAttrs, String customMod, List<AbilitySlot> abilitySlots, Type type) {
        this(level, name, description, Collections.emptyList(), requiredAttrs, customMod, abilitySlots, type);
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

    @Override
    public int compareTo(Ability o) {
        return this.toString().compareTo(o.toString());
    }

    public Type getType() {
        return type;
    }
}
