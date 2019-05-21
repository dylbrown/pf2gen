package model.abilities;

import model.AttributeMod;
import model.ability_scores.AbilityMod;
import model.enums.Type;

import java.util.Collections;
import java.util.List;

public class Ability {
    private final List<String> prerequisites;
    private List<Type> attributes;
    List<AttributeMod> modifiers;
    private List<AbilityMod> abilityMods;
    private final String name;
    private final String description;
    private final int level;

    private Ability(String name, int level,  String description, List<String> prerequisites) {
        this.name = name;
        this.description = description;
        this.prerequisites = prerequisites;
        this.level = level;
        abilityMods = Collections.emptyList();
    }

    Ability(int level, String name, String description, List<String> prerequisites) {
        this(name, level, description, prerequisites);
        this.modifiers= Collections.emptyList();
    }

    public Ability(int level, String name, List<AttributeMod> mods, String description, List<String> prerequisites) {
        this(name, level, description, prerequisites);
        this.modifiers = mods;
    }

    public Ability(int level, String name, List<AbilityMod> boosts, String description) {
        this(level, name, description, Collections.emptyList());
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

    public int getLevel() {
        return level;
    }
}
