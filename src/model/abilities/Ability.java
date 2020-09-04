package model.abilities;

import model.NamedObject;
import model.ability_scores.AbilityMod;
import model.ability_scores.AbilityScore;
import model.ability_slots.AbilitySlot;
import model.attributes.AttributeMod;
import model.attributes.AttributeRequirement;
import model.enums.Recalculate;
import model.enums.Trait;
import model.enums.Type;
import model.util.Pair;

import java.lang.reflect.Constructor;
import java.util.*;

import static model.util.Copy.copy;

public class Ability extends NamedObject implements Comparable<Ability> {
    //TODO: Support Repeated Choice
    private final List<String> prerequisites, prereqStrings, givenPrerequisites;
    private final List<AttributeRequirement> requiredAttrs;
    private final List<Pair<AbilityScore, Integer>> requiredScores;
    private final String customMod;
    private final List<AbilitySlot> abilitySlots;
    private final Type type;
    private final boolean multiple;
    private final Recalculate recalculate;
    private final List<AttributeMod> modifiers;
    private final List<AbilityMod> abilityMods;
    private final List<Trait> traits;
    private final String requirements;
    private final int level;
    private final int skillIncreases;
    private final Map<Class<? extends AbilityExtension>, AbilityExtension> extensions;

    private Ability(Builder builder, Map<Class<? extends AbilityExtension>, AbilityExtension> extensions) {
        super(builder);
        this.level = builder.level;
        this.modifiers = builder.modifiers;
        this.prerequisites = builder.prerequisites;
        this.prereqStrings = builder.prereqStrings;
        this.givenPrerequisites = builder.givenPrerequisites;
        this.requirements = builder.requirements;
        this.requiredAttrs = builder.requiredAttrs;
        this.requiredScores = builder.requiredScores;
        this.traits = builder.traits;
        this.customMod = builder.customMod;
        this.abilityMods = builder.abilityMods;
        this.abilitySlots = builder.abilitySlots;
        this.type = builder.type;
        this.multiple = builder.multiple;
        this.skillIncreases = builder.skillIncreases;
        this.recalculate = builder.recalculate;
        this.extensions = extensions;
    }

    private Ability(Ability.Builder builder) {
        this(builder, new HashMap<>());
        for (AbilityExtension.Builder extensionBuilder : builder.extensions.values()) {
            AbilityExtension extension = extensionBuilder.build(this);
            extensions.put(extension.getClass(), extension);
        }
    }

    public <T extends AbilityExtension> T getExtension(Class<T> extensionClass) {
        AbilityExtension extension = extensions.get(extensionClass);
        if(extensionClass.isInstance(extension))
            return extensionClass.cast(extension);
        else return null;
    }

    public AbilityExtension getExtensionByName(String extensionName) {
        extensionName = extensionName.toLowerCase();
        for (Map.Entry<Class<? extends AbilityExtension>, AbilityExtension> entry : extensions.entrySet()) {
            if(entry.getKey().getName().toLowerCase().contains(extensionName))
                return entry.getValue();
        }
        return null;
    }

    public boolean hasExtension(String extensionName) {
        extensionName = extensionName.toLowerCase();
        for (Class<? extends AbilityExtension> aClass : extensions.keySet()) {
            if(aClass.getName().toLowerCase().contains(extensionName))
                return true;
        }
        return false;
    }

    public List<AttributeMod> getModifiers() {
        return Collections.unmodifiableList(modifiers);
    }

    public List<AbilityMod> getAbilityMods() {
        return Collections.unmodifiableList(abilityMods);
    }

    public int getLevel() {
        return level;
    }

    public int getSkillIncreases() {
        return skillIncreases;
    }

    public List<AttributeRequirement> getRequiredAttrs() {
        return Collections.unmodifiableList(requiredAttrs);
    }

    public List<Pair<AbilityScore, Integer>> getRequiredScores() {
        return requiredScores;
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

    public String getRequirements() {
        return requirements;
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

    public Recalculate getRecalculate() {
        return recalculate;
    }

    public Type getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Ability ability = (Ability) o;
        return level == ability.level &&
                type == ability.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), type, level);
    }

    public List<Trait> getTraits() {
        return traits;
    }

    Ability copyWithLevel(Integer i) {
        Builder builder = new Builder(this);
        builder.setLevel(i);
        return new Ability(builder, this.extensions);
    }

    public static class Builder extends NamedObject.Builder {
        private List<String> prerequisites = Collections.emptyList();
        private List<String> prereqStrings = Collections.emptyList();
        private List<String> givenPrerequisites = Collections.emptyList();
        private List<AttributeRequirement> requiredAttrs = Collections.emptyList();
        private List<Pair<AbilityScore, Integer>> requiredScores = Collections.emptyList();
        private List<Trait> traits = Collections.emptyList();
        private String customMod = "";
        private List<AbilitySlot> abilitySlots = Collections.emptyList();
        private Type type;
        private boolean multiple = false;
        private List<AttributeMod> modifiers = Collections.emptyList();
        private List<AbilityMod> abilityMods = Collections.emptyList();
        private int level = 1;
        private int skillIncreases = 0;
        private String requirements = "";
        private Recalculate recalculate = Recalculate.Never;
        private Map<Class<? extends AbilityExtension.Builder>, AbilityExtension.Builder>
                extensions = Collections.emptyMap();

        public Builder(){}

        public Builder(Builder other) {
            this.prerequisites = copy(other.prerequisites);
            this.prereqStrings = copy(other.prereqStrings);
            this.givenPrerequisites = copy(other.givenPrerequisites);
            this.requiredAttrs = copy(other.requiredAttrs);
            this.requiredScores = copy(other.requiredScores);
            this.traits = copy(other.traits);
            this.customMod = other.customMod;
            this.abilitySlots = copy(other.abilitySlots);
            this.type = other.type;
            this.multiple = other.multiple;
            this.modifiers = copy(other.modifiers);
            this.abilityMods = copy(other.abilityMods);
            this.level = other.level;
            this.skillIncreases = other.skillIncreases;
            this.requirements = other.requirements;
            this.recalculate = other.recalculate;
            this.extensions = new HashMap<>();
            for (Map.Entry<Class<? extends AbilityExtension.Builder>,
                    AbilityExtension.Builder> entry : other.extensions.entrySet()) {
                extensions.put(entry.getKey(), copy(entry.getValue()));
            }
        }

        private Builder(Ability ability) {
            this.prerequisites = ability.prerequisites;
            this.prereqStrings = ability.prereqStrings;
            this.givenPrerequisites = ability.givenPrerequisites;
            this.requiredAttrs = ability.requiredAttrs;
            this.requiredScores = ability.requiredScores;
            this.traits = ability.traits;
            this.customMod = ability.customMod;
            this.abilitySlots = ability.abilitySlots;
            this.type = ability.type;
            this.multiple = ability.multiple;
            this.modifiers = ability.modifiers;
            this.abilityMods = ability.abilityMods;
            this.level = ability.level;
            this.skillIncreases = ability.skillIncreases;
            this.requirements = ability.requirements;
            this.recalculate = ability.recalculate;
        }

        public void setPrerequisites(List<String> prerequisites) {
            this.prerequisites = prerequisites;
        }

        public void setRequirements(String requirements) {
            this.requirements = requirements;
        }

        public void setPrereqStrings(List<String> strings) {
            this.prereqStrings = strings;
        }

        public void setGivesPrerequisites(List<String> given) {this.givenPrerequisites = given;}

        public void setRequiredAttrs(List<AttributeRequirement> requiredAttrs) {
            if(requiredAttrs.isEmpty())
                this.requiredAttrs = Collections.emptyList();
            else
                this.requiredAttrs = requiredAttrs;
        }

        public void setRequiredScores(List<Pair<AbilityScore, Integer>> requiredScores) {
            if(requiredScores.isEmpty())
                this.requiredScores = Collections.emptyList();
            else
                this.requiredScores = requiredScores;
        }

        public void setCustomMod(String customMod) {
            this.customMod = customMod;
        }

        public void addAbilitySlot(AbilitySlot abilitySlot) {
            if(abilitySlots.size() == 0) abilitySlots = new ArrayList<>();
            abilitySlots.add(abilitySlot);
        }

        public void addTraits(Trait... traits) {
            if(traits.length == 0) return;
            if(this.traits.isEmpty()) this.traits = new ArrayList<>();
            this.traits.addAll(Arrays.asList(traits));
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

        public void setLevel(int level) {
            this.level = level;
        }

        public void setSkillIncreases(int skillIncreases) {
            this.skillIncreases = skillIncreases;
        }

        public void setRecalculateMod(Recalculate recalculate) {
            this.recalculate = recalculate;
        }

        public <T extends AbilityExtension.Builder> T getExtension(Class<T> extensionClass) {
            AbilityExtension.Builder extension = extensions.get(extensionClass);
            if(extensionClass.isInstance(extension))
                return extensionClass.cast(extension);
            else {
                if(extensions.size() == 0) extensions = new HashMap<>();
                try {
                    Constructor<T> defaultConstructor = extensionClass.getDeclaredConstructor();
                    T newExtension = defaultConstructor.newInstance();
                    extensions.put(extensionClass, newExtension);
                    return newExtension;
                } catch (ReflectiveOperationException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }

        public <T extends AbilityExtension.Builder> boolean hasExtension(Class<T> extensionClass) {
            return extensionClass.isInstance(extensions.get(extensionClass));
        }

        public Ability build() {
            return new Ability(this);
        }

        public List<Trait> getTraits() {
            return Collections.unmodifiableList(traits);
        }
    }
}
