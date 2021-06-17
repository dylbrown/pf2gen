package model.items;

import model.AbstractNamedObject;
import model.abilities.Ability;
import model.attributes.AttributeBonus;
import model.enums.Recalculate;
import model.enums.Slot;
import model.enums.Trait;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class BaseItem extends AbstractNamedObject implements Item {
    private final double weight, value;
    private final String category, subCategory, customMod;
    private final Slot slot;
    private final List<Trait> traits;
    private final Recalculate recalculate;
    private final int hands, level;
    private final List<AttributeBonus> bonuses;
    private final List<Ability> abilities;
    private final Map<Class<? extends ItemExtension>, ItemExtension> extensions;

    protected BaseItem(BaseItem.Builder builder) {
        super(builder);
        this.weight = builder.weight;
        this.value = builder.value;
        this.category = builder.category;
        this.subCategory = builder.subCategory;
        this.slot = builder.slot;
        this.traits = (builder.traits.size() > 0) ? builder.traits : Collections.emptyList();
        this.customMod = builder.customMod;
        this.recalculate = builder.recalculate;
        this.hands = builder.hands;
        this.level = builder.level;
        this.bonuses = builder.bonuses;
        this.abilities = builder.abilities;
        this.extensions = new HashMap<>();
        for (ItemExtension.Builder extensionBuilder : builder.extensions.values()) {
            ItemExtension extension = extensionBuilder.build(this);
            extensions.put(extension.getClass(), extension);
        }
    }

    public <T extends ItemExtension> T getExtension(Class<T> extensionClass) {
        ItemExtension extension = extensions.get(extensionClass);
        if(extensionClass.isInstance(extension))
            return extensionClass.cast(extension);
        else return null;
    }

    public Collection<ItemExtension> getExtensions() {
        return Collections.unmodifiableCollection(extensions.values());
    }

    public ItemExtension getExtensionByName(String extensionName) {
        extensionName = extensionName.toLowerCase();
        for (Map.Entry<Class<? extends ItemExtension>, ItemExtension> entry : extensions.entrySet()) {
            if(entry.getKey().getName().toLowerCase().contains(extensionName))
                return entry.getValue();
        }
        return null;
    }

    public <T extends ItemExtension> boolean hasExtension(Class<T> extensionClass) {
        ItemExtension extension = extensions.get(extensionClass);
        return extensionClass.isInstance(extension);
    }

    public boolean hasExtension(String extensionName) {
        extensionName = extensionName.toLowerCase();
        for (Class<? extends ItemExtension> aClass : extensions.keySet()) {
            if(aClass.getSimpleName().toLowerCase().matches(extensionName+"(extension)?"))
                return true;
        }
        return false;
    }

    public double getWeight() {
        return weight;
    }

    public double getValue() {
        return value;
    }

    public String getPrettyWeight() {
        if(weight == 0) return "";
        if(weight >= 1)
            return String.valueOf(Math.round(weight));
        else
            return ((weight != .1) ? Math.floor(weight)*10 : "") +"L";
    }

    public Slot getSlot() {
        return resolveDecoration(slot, "getSlot", Slot.class);
    }

    public int getHands() {
        return hands;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public String getName() {
        return resolveDecoration(super.getName(), "getName");
    }

    @Override
    public String getDescription() {
        return resolveDecoration(super.getDescription(), "getDescription");
    }

    @Override
    public String toString() {
        return getName();
    }

    public String getCategory() {return resolveDecoration(category, "getCategory");}

    public String getSubCategory() {return resolveDecoration(subCategory, "getSubCategory");}

    public Item copy() {
        Builder builder = new Builder(this);
        return builder.build();
    }

    public List<Trait> getTraits() {
        return Collections.unmodifiableList(traits);
    }

    @Override
    public String getCustomMod() {
        return customMod;
    }

    @Override
    public Recalculate getRecalculate() {
        return recalculate;
    }

    public List<AttributeBonus> getBonuses() {
        return Collections.unmodifiableList(bonuses);
    }

    public List<Ability> getAbilities() {
        return Collections.unmodifiableList(abilities);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        BaseItem item = (BaseItem) o;
        return Double.compare(item.weight, weight) == 0 &&
                Double.compare(item.value, value) == 0 &&
                hands == item.hands &&
                level == item.level &&
                Objects.equals(category, item.category) &&
                Objects.equals(subCategory, item.subCategory) &&
                slot == item.slot &&
                Objects.equals(traits, item.traits) &&
                Objects.equals(bonuses, item.bonuses) &&
                Objects.equals(abilities, item.abilities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), weight, value, category, subCategory, slot, traits, hands, level, bonuses, abilities);
    }

    @Override
    public int compareTo(Item o) {
        return this.getName().compareTo(o.getName());
    }

    private String resolveDecoration(String value, String functionName) {
        return resolveDecoration(value, functionName, String.class);
    }

    <T> T resolveDecoration(T value, String functionName, Class<T> valueClass) {
        for (ItemExtension extension : extensions.values()) {
            try {
                Method method = extension.getClass().getMethod(functionName, valueClass);
                value = valueClass.cast(method.invoke(extension, value));
            } catch (NoSuchMethodException ignored) {
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return value;
    }

    public static class Builder extends AbstractNamedObject.Builder {
        private double weight = 0;
        private double value = 0;
        private String category = "";
        private String subCategory = "";
        private Slot slot = Slot.None;
        private List<Trait> traits = new ArrayList<>();
        private String customMod = "";
        private Recalculate recalculate = Recalculate.Never;
        private int hands;
        private int level;
        private List<AttributeBonus> bonuses = Collections.emptyList();
        private List<Ability> abilities = Collections.emptyList();
        private Map<Class<? extends ItemExtension.Builder>, ItemExtension.Builder>
                extensions = Collections.emptyMap();

        public Builder() {}

        public Builder(BaseItem item) {
            this.weight = item.weight;
            this.value = item.value;
            this.category = item.category;
            this.subCategory = item.subCategory;
            this.slot = item.slot;
            this.traits = (item.traits.size() > 0) ? new ArrayList<>(item.traits) : Collections.emptyList();
            this.hands = item.hands;
            this.level = item.level;
            this.bonuses = new ArrayList<>(item.bonuses);
            this.abilities = new ArrayList<>(item.abilities);
        }

        public void setWeight(double weight) {
            this.weight = weight;
        }

        public void setValue(double value) {
            this.value = value;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public void setSubCategory(String subCategory) {
            this.subCategory = subCategory;
        }

        public void setSlot(Slot slot) {
            this.slot = slot;
        }

        public void addTrait(Trait trait) {
            traits.add(trait);
        }

        public void setCustomMod(String customMod) {
            this.customMod = customMod;
        }

        public void setRecalculate(Recalculate recalculate) {
            this.recalculate = recalculate;
        }

        public void setHands(int hands) {
            this.hands = hands;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public void addBonus(AttributeBonus bonus) {
            if(bonuses.size() == 0) bonuses = new ArrayList<>();
            bonuses.add(bonus);
        }

        public void addAbility(Ability ability) {
            if(abilities.size() == 0) abilities = new ArrayList<>();
            abilities.add(ability);
        }

        public <T extends ItemExtension.Builder> T getExtension(Class<T> extensionClass) {
            ItemExtension.Builder extension = extensions.get(extensionClass);
            if(extensionClass.isInstance(extension))
                return extensionClass.cast(extension);
            else {
                if(extensions.size() == 0) extensions = new HashMap<>();
                try {
                    for (Constructor<?> constructor : extensionClass.getDeclaredConstructors()) {
                        if(constructor.getParameterCount() == 1 &&
                                constructor.getParameterTypes()[0] == BaseItem.Builder.class) {
                            T newExtension = extensionClass.cast(constructor.newInstance(this));
                            extensions.put(extensionClass, newExtension);
                            return newExtension;
                        }
                    }
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

        public <T extends ItemExtension.Builder> boolean hasExtension(Class<T> extensionClass) {
            return extensionClass.isInstance(extensions.get(extensionClass));
        }

        public Item build() {
            return new BaseItem(this);
        }
    }
}
