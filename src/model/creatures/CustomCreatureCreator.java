package model.creatures;

import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import model.abilities.Ability;
import model.ability_scores.AbilityScore;
import model.attributes.Attribute;
import model.attributes.BaseAttribute;
import model.creatures.scaling.ScaleMap;
import model.enums.Alignment;
import model.enums.Language;
import model.enums.Size;
import model.enums.Trait;
import model.player.ArbitraryChoice;
import model.player.SourcesManager;
import model.util.*;

import java.util.*;

public class CustomCreatureCreator {
    public final StringProperty name = new SimpleStringProperty("");
    public final ReadOnlyIntegerWrapper level = new ReadOnlyIntegerWrapper(0);
    public final ObjectProperty<Alignment> alignment = new SimpleObjectProperty<>();
    public final ObjectProperty<Size> size = new SimpleObjectProperty<>();
    private String description = "";
    private CreatureFamily family;
    private final ObservableList<ObservableValue<Trait>> traits = FXCollections.observableArrayList();
    public final Map<Attribute, CustomCreatureValue<Attribute>> modifiers = new HashMap<>();
    private final ObservableList<CustomCreatureValue<AbilityScore>> abilityModifiers = FXCollections.observableArrayList();
    private final ObservableList<CustomCreatureValue<Attribute>> skills = FXCollections.observableArrayList();
    private final List<Language> languages = new ArrayList<>();
    private final List<CreatureItem> items = new ArrayList<>();
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
    private final ArbitraryChoice<Attribute> skillsChoice;


    public CustomCreatureCreator(SourcesManager sources) {
        this.sources = sources;
        addTraitHookup(alignment);
        addTraitHookup(size);
        AbilityScore.scores().forEach(a->abilityModifiers.add(
                new CustomCreatureValue<>(a, AbilityScore::toFullName, level.getReadOnlyProperty(), ScaleMap.ABILITY_MODIFIER_SCALES)));

        ArbitraryChoice.Builder<Attribute> skillsChoiceBuilder = new ArbitraryChoice.Builder<>();
        skillsChoiceBuilder.setChoicesConstant(Arrays.asList(BaseAttribute.getSkills()));
        skillsChoiceBuilder.setMaxSelections(-1);
        skillsChoiceBuilder.setOptionsClass(Attribute.class);
        skillsChoiceBuilder.setName("Choose Skills");
        skillsChoice = skillsChoiceBuilder.build();

        skillsChoice.getSelections().addListener((ListChangeListener<Attribute>) change -> {
            while(change.next()) {
                for (Attribute attribute : change.getAddedSubList()) {
                    skills.add(getOrCreateModifier(attribute));
                }
                for (Attribute attribute : change.getRemoved()) {
                    modifiers.remove(attribute);
                    skills.removeIf(v->v.target == attribute);
                }
            }
        });
    }

    private <T> void addTraitHookup(ObjectProperty<T> property) {
        traits.add(new TransformationProperty<>(property, a-> {
            try {
                if(a != null) return sources.traits().find(a.toString());
            } catch (ObjectNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }));
    }

    public CustomCreatureValue<Attribute> getOrCreateModifier(Attribute attribute) {
        return modifiers.computeIfAbsent(attribute, a->new CustomCreatureValue<>(a, level, ScaleMap.get(attribute.getBase())));
    }

    public CustomCreature getCreature() {
        return creature;
    }

    public ObservableList<CustomCreatureValue<AbilityScore>> getAbilityScores() {
        return abilityModifiers;
    }

    public ArbitraryChoice<Attribute> getSkillsChoice() {
        return skillsChoice;
    }

    private final ObservableList<CustomCreatureValue<Attribute>> unmodifiableSkills =
            FXCollections.unmodifiableObservableList(skills);
    public ObservableList<CustomCreatureValue<Attribute>> getSkills() {
        return unmodifiableSkills;
    }

    private class CustomCreature implements Creature {

        private final TransformationMap<AbilityScore, CustomCreatureValue<AbilityScore>, Integer> abilityModifierInts;
        private final TransformationMap<Attribute, CustomCreatureValue<Attribute>, CreatureValue<Attribute>> attributeMods;
        private final WrapperTransformationList<Trait, ObservableValue<Trait>> traitsList;

        public CustomCreature() {
            this.abilityModifierInts = new TransformationMap<>(new ListToMap<>(abilityModifiers, a->a.target), CustomCreatureValue::getModifier);
            attributeMods = new TransformationMap<>(modifiers, CustomCreatureValue::getAsCreatureValue);
            traitsList = new WrapperTransformationList<>(traits, ObservableValue::getValue);
        }

        public CreatureFamily getFamily() {
            return family;
        }

        public List<Trait> getTraits() {
            return Collections.unmodifiableList(traitsList);
        }

        public Map<Attribute, CreatureValue<Attribute>> getModifiers() {
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
            return level.get();
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
            return name.get();
        }

        @Override
        public String getRawName() {
            return name.get();
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
