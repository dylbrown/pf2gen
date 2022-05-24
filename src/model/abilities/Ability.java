package model.abilities;

import model.AbstractNamedObject;
import model.ability_scores.AbilityScore;
import model.ability_slots.AbilitySlot;
import model.attributes.Attribute;
import model.data_managers.sources.Source;
import model.enums.Recalculate;
import model.enums.Trait;
import model.enums.Type;
import model.util.Pair;

import java.lang.reflect.Constructor;
import java.util.*;

import static model.util.Copy.copy;

public class Ability extends AbstractNamedObject implements Comparable<Ability> {
    //TODO: Support Repeated Choice
    private final List<String> prerequisites, prereqStrings, givenPrerequisites;
    private final Requirement<Attribute> requiredAttrs;
    private final Requirement<String> requiredWeapons;
    private final List<Pair<AbilityScore, Integer>> requiredScores;
    private final String customMod;
    private final List<AbilitySlot> abilitySlots;
    private final Type type;
    private final boolean multiple;
    private final Recalculate recalculate;
    private final List<Trait> traits;
    private final String requirements;
    private final int level;
    private final Map<Class<? extends AbilityExtension>, AbilityExtension> extensions;

    private Ability(Builder builder, Map<Class<? extends AbilityExtension>, AbilityExtension> extensions) {
        super(builder);
        this.level = builder.level;
        this.prerequisites = builder.prerequisites;
        this.prereqStrings = builder.prereqStrings;
        this.givenPrerequisites = builder.givenPrerequisites;
        this.requirements = builder.requirements;
        this.requiredAttrs = builder.requiredAttrs;
        this.requiredWeapons = builder.requiredWeapons;
        this.requiredScores = builder.requiredScores;
        this.traits = builder.traits;
        this.customMod = builder.customMod;
        this.abilitySlots = builder.abilitySlots;
        this.type = builder.type;
        this.multiple = builder.multiple;
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

    public Collection<AbilityExtension> getExtensions() {
        return Collections.unmodifiableCollection(extensions.values());
    }

    public boolean hasExtension(String extensionName) {
        extensionName = extensionName.toLowerCase();
        for (Class<? extends AbilityExtension> aClass : extensions.keySet()) {
            if(aClass.getSimpleName().toLowerCase().matches(extensionName+"(extension)?"))
                return true;
        }
        return false;
    }

    public int getLevel() {
        return level;
    }

    public Requirement<Attribute> getRequiredAttrs() {
        return (requiredAttrs != null) ? requiredAttrs : Requirement.none();
    }

    public Requirement<String> getRequiredWeapons() {
        return (requiredWeapons != null) ? requiredWeapons : Requirement.none();
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

    public static class Builder extends AbstractNamedObject.Builder {
        private List<String> prerequisites = Collections.emptyList();
        private List<String> prereqStrings = Collections.emptyList();
        private List<String> givenPrerequisites = Collections.emptyList();
        private Requirement<Attribute> requiredAttrs = null;
        private Requirement<String> requiredWeapons = null;
        private List<Pair<AbilityScore, Integer>> requiredScores = Collections.emptyList();
        private List<Trait> traits = Collections.emptyList();
        private String customMod = "";
        private List<AbilitySlot> abilitySlots = Collections.emptyList();
        private Type type = Type.Untyped;
        private boolean multiple = false;
        private int level = 1;
        private String requirements = "";
        private Recalculate recalculate = Recalculate.Never;
        private Map<Class<? extends AbilityExtension.Builder>, AbilityExtension.Builder>
                extensions = Collections.emptyMap();

        public Builder(Builder other) {
            super(other.source);
            this.prerequisites = copy(other.prerequisites);
            this.prereqStrings = copy(other.prereqStrings);
            this.givenPrerequisites = copy(other.givenPrerequisites);
            this.requiredAttrs = other.requiredAttrs;
            this.requiredWeapons = other.requiredWeapons;
            this.requiredScores = copy(other.requiredScores);
            this.traits = copy(other.traits);
            this.customMod = other.customMod;
            this.abilitySlots = copy(other.abilitySlots);
            this.type = other.type;
            this.multiple = other.multiple;
            this.level = other.level;
            this.requirements = other.requirements;
            this.recalculate = other.recalculate;
            this.extensions = new HashMap<>();
            for (Map.Entry<Class<? extends AbilityExtension.Builder>,
                    AbilityExtension.Builder> entry : other.extensions.entrySet()) {
                extensions.put(entry.getKey(), copy(entry.getValue()));
            }
        }

        private Builder(Ability ability) {
            super(ability.getSourceBook());
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
            this.level = ability.level;
            this.requirements = ability.requirements;
            this.recalculate = ability.recalculate;
        }

        public Builder(Source source) {
            super(source);
        }

        public void addPrerequisite(String prerequisite) {
            if(prerequisites.isEmpty())
                prerequisites = new ArrayList<>();
            prerequisites.add(prerequisite);
        }

        public void setRequirements(String requirements) {
            this.requirements = requirements;
        }

        public void setPrereqStrings(List<String> strings) {
            this.prereqStrings = strings;
        }

        public void setGivesPrerequisites(List<String> given) {this.givenPrerequisites = given;}

        public void addRequiredAttr(Requirement<Attribute> requiredAttrs) {
            if(this.requiredAttrs == null)
                this.requiredAttrs = requiredAttrs;
            else
                this.requiredAttrs = new RequirementList<>(Arrays.asList(this.requiredAttrs, requiredAttrs), true);
        }

        public void addRequiredWeapon(Requirement<String> requiredWeapon) {
            if(this.requiredWeapons == null)
                this.requiredWeapons = requiredWeapon;
            else
                this.requiredWeapons = new RequirementList<>(Arrays.asList(this.requiredWeapons, requiredWeapon), true);
        }

        public void addRequiredScore(Pair<AbilityScore, Integer> requiredScore) {
            if(requiredScores.isEmpty())
                this.requiredScores = new ArrayList<>();
            requiredScores.add(requiredScore);
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

        public void setType(Type type) {
            if(type != Type.Untyped && type != null)
                this.type = type;
        }

        public void setMultiple(boolean multiple) {
            this.multiple = multiple;
        }

        public void setLevel(int level) {
            this.level = level;
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

        public Type getType() {
            return type;
        }
    }
}
