package model.abilities;

import model.AttributeMod;
import model.abilities.abilitySlots.AbilitySlot;
import model.ability_scores.AbilityMod;
import model.enums.Type;

import java.util.*;

public class Ability implements Comparable<Ability> {
    //TODO: Support Repeatedly Chooseable
    private final List<String> prerequisites, prereqStrings, givenPrerequisites;
    private final List<AttributeMod> requiredAttrs;
    private final String customMod;
    private final List<AbilitySlot> abilitySlots;
    private final Type type;
    private final boolean multiple;
    private final List<AttributeMod> modifiers;
    private final List<AbilityMod> abilityMods;
    private final String name;
    private final String description;
    private final int level;
    private final int skillIncreases;


    protected Ability(Ability.Builder builder) {
        this.name = builder.name;
        this.level = builder.level;
        this.modifiers = builder.modifiers;
        this.description = builder.description;
        this.prerequisites = builder.prerequisites;
        this.prereqStrings = builder.prereqStrings;
        this.givenPrerequisites = builder.givenPrerequisites;
        if(builder.requiredAttrs.size() == 0)
            this.requiredAttrs = Collections.emptyList();
        else
            this.requiredAttrs = builder.requiredAttrs;
        this.customMod = builder.customMod;
        this.abilityMods = builder.abilityMods;
        this.abilitySlots = builder.abilitySlots;
        this.type = builder.type;
        this.multiple = builder.multiple;
        this.skillIncreases = builder.skillIncreases;
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

    public int getSkillIncreases() {
        return skillIncreases;
    }

    public List<AttributeMod> getRequiredAttrs() {
        return Collections.unmodifiableList(requiredAttrs);
    }

    public List<String> getPrerequisites() {
        return Collections.unmodifiableList(prerequisites);
    }

    public List<String> getPrereqStrings() {
        return Collections.unmodifiableList(prereqStrings);
    }

    public List<String> getGivenPrerequisites() {
        return Collections.unmodifiableList(givenPrerequisites);
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

    public boolean isMultiple() {
        return multiple;
    }

    public Type getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ability ability = (Ability) o;
        return type == ability.type &&
                name.equals(ability.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name);
    }

    public static class Builder {
        private List<String> prerequisites = Collections.emptyList();
        private List<String> prereqStrings = Collections.emptyList();
        private List<String> givenPrerequisites = Collections.emptyList();
        private List<AttributeMod> requiredAttrs = Collections.emptyList();
        private String customMod = "";
        private List<AbilitySlot> abilitySlots = Collections.emptyList();
        private Type type;
        private boolean multiple = false;
        List<AttributeMod> modifiers = Collections.emptyList();
        private List<AbilityMod> abilityMods = Collections.emptyList();
        private String name;
        private String description = "";
        private int level = 1;
        private int skillIncreases = 0;

        public Builder(){}

        public Builder(Ability.Builder builder) {
            this.prerequisites = builder.prerequisites;
            this.prereqStrings = builder.prereqStrings;
            this.givenPrerequisites = builder.givenPrerequisites;
            this.requiredAttrs = builder.requiredAttrs;
            this.customMod = builder.customMod;
            this.abilitySlots = builder.abilitySlots;
            this.type = builder.type;
            this.multiple = builder.multiple;
            this.modifiers = builder.modifiers;
            this.abilityMods = builder.abilityMods;
            this.name = builder.name;
            this.description = builder.description;
            this.level = builder.level;
            this.skillIncreases = builder.skillIncreases;
        }

        public void setPrerequisites(List<String> prerequisites) {
            this.prerequisites = prerequisites;
        }

        public void setPrereqStrings(List<String> strings) {
            this.prereqStrings = strings;
        }

        public void setGivesPrerequisites(List<String> given) {this.givenPrerequisites = given;}

        public void setRequiredAttrs(List<AttributeMod> requiredAttrs) {
            this.requiredAttrs = requiredAttrs;
        }

        public void setCustomMod(String customMod) {
            this.customMod = customMod;
        }

        public void addAbilitySlot(AbilitySlot abilitySlot) {
            if(abilitySlots.size() == 0) abilitySlots = new ArrayList<>();
            abilitySlots.add(abilitySlot);
        }

        public void setAbilitySlots(List<AbilitySlot> abilitySlots) {
            this.abilitySlots = abilitySlots;
        }

        public void setType(Type type) {
            this.type = type;
        }

        public void setMultiple(boolean multiple) {
            this.multiple = multiple;
        }

        public void setAttrMods(List<AttributeMod> modifiers) {
            this.modifiers = modifiers;
        }

        public void setBoosts(List<AbilityMod> abilityMods) {
            this.abilityMods = abilityMods;
        }

        public void addAllMods(List<AttributeMod> mods) {
            if(modifiers.size() == 0) modifiers = new ArrayList<>();
            modifiers.addAll(mods);
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public void setSkillIncreases(int skillIncreases) {
            this.skillIncreases = skillIncreases;
        }

        public Ability build() {
            return new Ability(this);
        }
    }
}
