package model.equipment;

import model.abilities.Ability;
import model.attributes.AttributeBonus;
import model.enums.Rarity;
import model.enums.Slot;
import model.enums.Trait;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class ItemInstance implements Item {
    private final Map<Class<? extends ItemExtension>, ItemExtension> extensions = new HashMap<>();

    private final BaseItem item;

    public Item getSourceItem() {
        return item;
    }

    public ItemInstance(BaseItem item) {
        this.item = item;
    }

    public <T extends ItemExtension> T addExtension(Class<T> extensionClass) {
        try {
            Constructor<T> constructor = extensionClass.getDeclaredConstructor(Item.class);
            T newExtension = constructor.newInstance(this);
            extensions.put(extensionClass, newExtension);
            return newExtension;
        } catch (NoSuchMethodException | IllegalAccessException
                | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public <T extends ItemExtension> T getExtension(Class<T> extensionClass) {
        if(extensions.containsKey(extensionClass)) {
            ItemExtension extension = extensions.get(extensionClass);
            if(extensionClass.isInstance(extension))
                return extensionClass.cast(extension);
        }
        return item.getExtension(extensionClass);
    }

    public Collection<ItemExtension> getExtensions() {
        List<ItemExtension> extensionList = new ArrayList<>(extensions.values());
        extensionList.addAll(item.getExtensions());
        return extensionList;
    }

    @Override
    public ItemExtension getExtensionByName(String extensionName) {
        return item.getExtensionByName(extensionName);
    }

    @Override
    public <T extends ItemExtension> boolean hasExtension(Class<T> extensionClass) {
        if(extensions.containsKey(extensionClass))
            return true;
        return item.hasExtension(extensionClass);
    }

    @Override
    public boolean hasExtension(String extensionName) {
        return item.hasExtension(extensionName);
    }

    @Override
    public double getWeight() {
        return item.getWeight();
    }

    @Override
    public double getValue() {
        return resolveDecoration(item.getValue(), "getValue", Double.class);
    }

    @Override
    public Rarity getRarity() {
        return item.getRarity();
    }

    @Override
    public String getPrettyWeight() {
        return item.getPrettyWeight();
    }

    @Override
    public Slot getSlot() {
        return resolveDecoration(item.getSlot(), "getSlot", Slot.class);
    }

    @Override
    public int getHands() {
        return item.getHands();
    }

    @Override
    public int getLevel() {
        return item.getLevel();
    }

    @Override
    public String getCategory() {
        return item.getCategory();
    }

    @Override
    public String getSubCategory() {
        return item.getSubCategory();
    }

    @Override
    public String getName() {
        return resolveDecoration(item.getName(), "getName");
    }

    @Override
    public String getDescription() {
        return resolveDecoration(item.getDescription(), "getDescription");
    }

    @Override
    public Item copy() {
        return item.copy();
    }

    @Override
    public List<Trait> getTraits() {
        return item.getTraits();
    }

    @Override
    public List<AttributeBonus> getBonuses() {
        return item.getBonuses();
    }

    @Override
    public List<Ability> getAbilities() {
        return item.getAbilities();
    }

    @Override
    public int compareTo(Item o) {
        return item.compareTo(o);
    }

    @Override
    public String toString() {
        return getName();
    }

    private String resolveDecoration(String value, String functionName) {
        return resolveDecoration(value, functionName, String.class);
    }

    <T> T resolveDecoration(T value, String functionName, Class<T> valueClass) {
        value = item.resolveDecoration(value, functionName, valueClass);
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
}
