package model;

import model.abilities.Ability;
import model.ability_scores.AbilityScore;
import model.attributes.Attribute;
import model.enums.Language;
import model.enums.Trait;
import model.equipment.Equipment;
import model.spells.Spell;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Creature {
    private final List<Trait> traits;
    private final Map<Attribute, Integer> modifiers;
    private final Map<AbilityScore, Integer> abilityModifiers;
    private final List<Language> languages;
    private final List<Equipment> items;
    private final int ac, hp;
    private final String name, speed, saveMods, healthMods, immunities, resistances, weaknesses;
    private final List<Ability> miscAbilities, defensiveAbilities, offensiveAbilities;
    private final List<Attack> attacks;
    private final List<Spell> spells;

    private Creature(Builder builder) {
        traits = Collections.unmodifiableList(builder.traits);
        modifiers = Collections.unmodifiableMap(builder.modifiers);
        abilityModifiers = Collections.unmodifiableMap(builder.abilityModifiers);
        languages = Collections.unmodifiableList(builder.languages);
        items = Collections.unmodifiableList(builder.items);
        ac = builder.ac;
        hp = builder.hp;
        name = builder.name;
        speed = builder.speed;
        saveMods = builder.saveMods;
        healthMods = builder.healthMods;
        immunities = builder.immunities;
        resistances = builder.resistances;
        weaknesses = builder.weaknesses;
        miscAbilities = Collections.unmodifiableList(builder.miscAbilities);
        defensiveAbilities = Collections.unmodifiableList(builder.defensiveAbilities);
        offensiveAbilities = Collections.unmodifiableList(builder.offensiveAbilities);
        attacks = Collections.unmodifiableList(builder.attacks);
        spells = Collections.unmodifiableList(builder.spells);
    }

    public List<Trait> getTraits() {
        return traits;
    }

    public Map<Attribute, Integer> getModifiers() {
        return modifiers;
    }

    public Map<AbilityScore, Integer> getAbilityModifiers() {
        return abilityModifiers;
    }

    public List<Language> getLanguages() {
        return languages;
    }

    public List<Equipment> getItems() {
        return items;
    }

    public int getAc() {
        return ac;
    }

    public int getHp() {
        return hp;
    }

    public String getName() {
        return name;
    }

    public String getSpeed() {
        return speed;
    }

    public String getSaveMods() {
        return saveMods;
    }

    public String getHealthMods() {
        return healthMods;
    }

    public String getImmunities() {
        return immunities;
    }

    public String getResistances() {
        return resistances;
    }

    public String getWeaknesses() {
        return weaknesses;
    }

    public List<Ability> getDefensiveAbilities() {
        return defensiveAbilities;
    }

    public List<Ability> getOffensiveAbilities() {
        return offensiveAbilities;
    }

    public List<Attack> getAttacks() {
        return attacks;
    }

    public List<Spell> getSpells() {
        return spells;
    }

    @Override
    public String toString() {
        return getName();
    }

    public static class Builder {
        private List<Trait> traits = Collections.emptyList();
        private Map<Attribute, Integer> modifiers = Collections.emptyMap();
        private Map<AbilityScore, Integer> abilityModifiers = Collections.emptyMap();
        private List<Language> languages= Collections.emptyList();
        private List<Equipment> items= Collections.emptyList();
        private int ac = 0;
        private int hp = 0;
        private String name, speed, saveMods, healthMods, immunities, resistances, weaknesses;
        private List<Ability> miscAbilities = Collections.emptyList();
        private List<Ability> defensiveAbilities = Collections.emptyList();
        private List<Ability> offensiveAbilities = Collections.emptyList();
        private List<Attack> attacks= Collections.emptyList();
        private List<Spell> spells= Collections.emptyList();

        public void setTraits(List<Trait> traits) {
            this.traits = traits;
        }

        public void setModifiers(Map<Attribute, Integer> modifiers) {
            this.modifiers = modifiers;
        }

        public void setAbilityModifiers(Map<AbilityScore, Integer> abilityModifiers) {
            this.abilityModifiers = abilityModifiers;
        }

        public void setLanguages(List<Language> languages) {
            this.languages = languages;
        }

        public void setItems(List<Equipment> items) {
            this.items = items;
        }

        public void setAc(int ac) {
            this.ac = ac;
        }

        public void setHp(int hp) {
            this.hp = hp;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setSpeed(String speed) {
            this.speed = speed;
        }

        public void setSaveMods(String saveMods) {
            this.saveMods = saveMods;
        }

        public void setHealthMods(String healthMods) {
            this.healthMods = healthMods;
        }

        public void setImmunities(String immunities) {
            this.immunities = immunities;
        }

        public void setResistances(String resistances) {
            this.resistances = resistances;
        }

        public void setWeaknesses(String weaknesses) {
            this.weaknesses = weaknesses;
        }

        public void setMiscAbilities(List<Ability> miscAbilities) {
            this.miscAbilities = miscAbilities;
        }

        public void setDefensiveAbilities(List<Ability> defensiveAbilities) {
            this.defensiveAbilities = defensiveAbilities;
        }

        public void setOffensiveAbilities(List<Ability> offensiveAbilities) {
            this.offensiveAbilities = offensiveAbilities;
        }

        public void setAttacks(List<Attack> attacks) {
            this.attacks = attacks;
        }

        public void setSpells(List<Spell> spells) {
            this.spells = spells;
        }

        public Creature build() {
            return new Creature(this);
        }
    }
}
