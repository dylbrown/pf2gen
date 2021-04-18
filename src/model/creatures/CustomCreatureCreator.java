package model.creatures;

import model.abilities.Ability;
import model.ability_scores.AbilityScore;
import model.attributes.Attribute;
import model.enums.Language;
import model.enums.Trait;
import model.player.SourcesManager;
import model.util.ObjectNotFoundException;
import model.util.TransformationMap;

import java.util.*;

public class CustomCreatureCreator {
    private String name = "";
    private String description = "";
    private CreatureFamily family;
    private final List<Trait> traits = new ArrayList<>();
    public final Map<Attribute, CustomCreatureAttribute> modifiers = new HashMap<>();
    private final Map<AbilityScore, CustomCreatureAttribute> abilityModifiers = new HashMap<>();
    private final List<Language> languages = new ArrayList<>();
    private final List<CreatureItem> items = new ArrayList<>();
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
    private final String senses = "";
    private final List<Ability> miscAbilities = new ArrayList<>();
    private final List<Ability> defensiveAbilities = new ArrayList<>();
    private final List<Ability> offensiveAbilities = new ArrayList<>();
    private final List<Attack> attacks = new ArrayList<>();
    private final List<CreatureSpellList> spells = new ArrayList<>();
    private final SourcesManager sources;
    private final CustomCreature creature = new CustomCreature();

    public CustomCreatureCreator(SourcesManager sources) {
        this.sources = sources;
    }

    public CustomCreatureAttribute getModifier(Attribute attribute) {
        return modifiers.computeIfAbsent(attribute, CustomCreatureAttribute::new);
    }

    public void set(CreatureFamily family) {
        this.family = family;
        Trait trait = null;
        try {
            trait = sources.traits().find(family.getName());
        } catch (ObjectNotFoundException e) {
            e.printStackTrace();
        }
        if(trait != null)
            traits.add(trait);
    }
    public void unset(CreatureFamily family) {
        this.family = null;
        Trait trait = null;
        try {
            trait = sources.traits().find(family.getName());
        } catch (ObjectNotFoundException e) {
            e.printStackTrace();
        }
        if(trait != null)
            traits.remove(trait);
    }

    public void addTrait(Trait trait) {
        if(!traits.contains(trait))
            traits.add(trait);
    }

    public void removeTrait(Trait trait) {
        traits.remove(trait);
    }

    public CustomCreature getCreature() {
        return creature;
    }

    private class CustomCreature implements Creature {

        private final TransformationMap<AbilityScore, CustomCreatureAttribute, Integer> abilityModifierInts;
        private final TransformationMap<Attribute, CustomCreatureAttribute, CreatureAttribute> attributeMods;

        public CustomCreature() {
            this.abilityModifierInts = new TransformationMap<>(abilityModifiers, CustomCreatureAttribute::getModifier);
            attributeMods = new TransformationMap<>(modifiers, CustomCreatureAttribute::getAsCreatureAttribute);
        }

        public CreatureFamily getFamily() {
            return family;
        }

        public List<Trait> getTraits() {
            return Collections.unmodifiableList(traits);
        }

        public Map<Attribute, CreatureAttribute> getModifiers() {
            return Collections.unmodifiableMap(attributeMods);
        }

        public Map<AbilityScore, Integer> getAbilityModifiers() {
            return Collections.unmodifiableMap(abilityModifierInts);
        }

        public List<Language> getLanguages() {
            return Collections.unmodifiableList(languages);
        }

        public String getSpecialLanguages() {
            return specialLanguages;
        }

        public List<CreatureItem> getItems() {
            return Collections.unmodifiableList(items);
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
            return Collections.unmodifiableList(miscAbilities);
        }

        public List<Ability> getDefensiveAbilities() {
            return Collections.unmodifiableList(defensiveAbilities);
        }

        public List<Ability> getOffensiveAbilities() {
            return Collections.unmodifiableList(offensiveAbilities);
        }

        public List<Attack> getAttacks() {
            return Collections.unmodifiableList(attacks);
        }

        public List<CreatureSpellList> getSpells() {
            return Collections.unmodifiableList(spells);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getRawName() {
            return name;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public String getRawDescription() {
            return description;
        }

        @Override
        public int getPage() {
            return -1;
        }

        @Override
        public String getSourceBook() {
            return "CUSTOM";
        }

        @Override
        public String getSource() {
            return "CUSTOM";
        }
    }
}
