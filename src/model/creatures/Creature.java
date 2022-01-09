package model.creatures;

import model.AbstractNamedObject;
import model.abilities.Ability;
import model.ability_scores.AbilityScore;
import model.attributes.Attribute;
import model.data_managers.sources.Source;
import model.enums.Language;
import model.enums.Trait;

import java.util.*;

public class Creature extends AbstractNamedObject {
    private final CreatureFamily family;
    private final List<Trait> traits;
    private final Map<Attribute, Integer> modifiers;
    private final Map<Attribute, String> modifierSpecialInfo;
    private final Map<AbilityScore, Integer> abilityModifiers;
    private final List<Language> languages;
    private final List<CreatureItem> items;
    private final int level, ac, hp;
    private final String specialLanguages, speed, acMods, saveMods, healthMods, immunities,
            resistances, weaknesses, senses;
    private final List<Ability> miscAbilities, defensiveAbilities, offensiveAbilities;
    private final List<Attack> attacks;
    private final List<CreatureSpellList> spells;

    private Creature(Builder builder) {
        super(builder);
        family = builder.family;
        traits = Collections.unmodifiableList(builder.traits);
        modifiers = Collections.unmodifiableMap(builder.modifiers);
        modifierSpecialInfo = Collections.unmodifiableMap(builder.modifierSpecialInfo);
        abilityModifiers = Collections.unmodifiableMap(builder.abilityModifiers);
        languages = Collections.unmodifiableList(builder.languages);
        items = Collections.unmodifiableList(builder.items);
        level = builder.level;
        ac = builder.ac;
        hp = builder.hp;
        specialLanguages = builder.specialLanguages;
        speed = builder.speed;
        acMods = builder.acMods;
        saveMods = builder.saveMods;
        healthMods = builder.healthMods;
        immunities = builder.immunities;
        resistances = builder.resistances;
        weaknesses = builder.weaknesses;
        senses = builder.senses;
        miscAbilities = Collections.unmodifiableList(builder.miscAbilities);
        defensiveAbilities = Collections.unmodifiableList(builder.defensiveAbilities);
        offensiveAbilities = Collections.unmodifiableList(builder.offensiveAbilities);
        attacks = Collections.unmodifiableList(builder.attacks);
        spells = Collections.unmodifiableList(builder.spells);
    }

    public CreatureFamily getFamily() {
        return family;
    }

    public List<Trait> getTraits() {
        return traits;
    }

    public Map<Attribute, Integer> getModifiers() {
        return modifiers;
    }

    public Map<Attribute, String> getModifierSpecialInfo() {
        return modifierSpecialInfo;
    }

    public Map<AbilityScore, Integer> getAbilityModifiers() {
        return abilityModifiers;
    }

    public List<Language> getLanguages() {
        return languages;
    }

    public String getSpecialLanguages() {
        return specialLanguages;
    }

    public List<CreatureItem> getItems() {
        return items;
    }

    public int getLevel() {
        return level;
    }

    public int getAC() {
        return ac;
    }

    public int getHP() {
        return hp;
    }

    public String getSpeed() {
        return speed;
    }

    public String getACMods() {
        return acMods;
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

    public String getSenses() {
        return senses;
    }

    public List<Ability> getMiscAbilities() {
        return miscAbilities;
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

    public List<CreatureSpellList> getSpells() {
        return spells;
    }

    @Override
    public String toString() {
        return getName();
    }

    public static class Builder extends AbstractNamedObject.Builder {
        private CreatureFamily family = null;
        private List<Trait> traits = Collections.emptyList();
        private Map<Attribute, Integer> modifiers = Collections.emptyMap();
        //TODO: Support Lore
        private Map<Attribute, String> modifierSpecialInfo = Collections.emptyMap();
        private Map<AbilityScore, Integer> abilityModifiers = Collections.emptyMap();
        private List<Language> languages= Collections.emptyList();
        private List<CreatureItem> items= Collections.emptyList();
        private int level = 0;
        private int ac = 0;
        private int hp = 0;
        private String specialLanguages = "";
        private String speed = "";
        private String acMods = "";
        private String saveMods = "";
        private String healthMods = "";
        private String immunities = "";
        private String resistances = "";
        private String weaknesses = "";
        private String senses = "";
        private List<Ability> miscAbilities = Collections.emptyList();
        private List<Ability> defensiveAbilities = Collections.emptyList();
        private List<Ability> offensiveAbilities = Collections.emptyList();
        private List<Attack> attacks= Collections.emptyList();
        private List<CreatureSpellList> spells= Collections.emptyList();

        public Builder(Source source) {
            super(source);
        }

        public void setFamily(CreatureFamily creatureFamily) {
            this.family = creatureFamily;
        }

        public void setTraits(List<Trait> traits) {
            this.traits = traits;
        }

        public void setModifier(Attribute attribute, int modifier) {
            if(modifiers.size() == 0)
                modifiers = new HashMap<>();
            modifiers.put(attribute, modifier);
        }

        public void addModifierSpecialInfo(Attribute attribute, String info) {
            if(modifierSpecialInfo.size() == 0)
                modifierSpecialInfo = new HashMap<>();
            modifierSpecialInfo.put(attribute, info);
        }

        public void setModifier(AbilityScore score, int modifier) {
            if(abilityModifiers.size() == 0)
                abilityModifiers = new HashMap<>();
            abilityModifiers.put(score, modifier);
        }

        public void setLanguages(List<Language> languages) {
            this.languages = languages;
        }

        public void setSpecialLanguages(String specialLanguages) {
            this.specialLanguages = specialLanguages;
        }

        public void addItem(CreatureItem item) {
            if(items.size() == 0)
                items = new ArrayList<>();
            this.items.add(item);
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public void setAC(int ac) {
            this.ac = ac;
        }

        public void setHP(int hp) {
            this.hp = hp;
        }

        public void setSpeed(String speed) {
            this.speed = speed;
        }

        public void setACMods(String acMods) {
            this.acMods = acMods;
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

        public void setSenses(String senses) {
            this.senses = senses;
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

        public List<CreatureSpellList> getSpells() {
            return Collections.unmodifiableList(spells);
        }

        public void addSpells(CreatureSpellList list) {
            if(spells.size() == 0)
                spells = new ArrayList<>();
            spells.add(list);
        }

        public Creature build() {
            return new Creature(this);
        }
    }
}
