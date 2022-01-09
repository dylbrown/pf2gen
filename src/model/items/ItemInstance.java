package model.items;

import model.abilities.Ability;
import model.attributes.AttributeBonus;
import model.enums.Recalculate;
import model.enums.Slot;
import model.enums.Trait;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class ItemInstance implements Item {
    private final Map<Class<? extends ItemExtension>, ItemExtension> extensions = new HashMap<>();

    private final BaseItem item;

    public BaseItem getSourceItem() {
        return item;
    }

    public ItemInstance(Item item) {
        if(item instanceof BaseItem)
            this.item = (BaseItem) item;
        else if(item instanceof ItemInstance)
            this.item = ((ItemInstance) item).getSourceItem();
        else this.item = null;

        for (ItemExtension extension : item.getExtensions()) {
            extension.applyToCreatedInstance(this);
        }
    }

    public <T extends ItemExtension> T addExtension(Class<T> extensionClass) {
        if(ItemInstanceExtension.class.isAssignableFrom(extensionClass)) {
            throw new UnsupportedOperationException("Adding an instance exception");
        }
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

    public <T extends ItemExtension, U extends ItemInstanceExtension<T>> U addInstanceExtension(Class<U> extensionClass, Class<T> parentExtension) {
        try {
            Constructor<U> constructor = extensionClass.getDeclaredConstructor(ItemInstance.class, parentExtension);
            U newExtension = constructor.newInstance(this, this.getExtension(parentExtension));
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
        return resolveDecoration(item.getRawName(), "getName");
    }

    @Override
    public String getRawName() {
        return item.getRawName();
    }

    @Override
    public String getSource() {
        return item.getSource();
    }

    @Override
    public String getDescription() {
        return resolveDecoration(item.getRawDescription(), "getDescription");
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
    public String getCustomMod() {
        return item.getCustomMod();
    }

    @Override
    public Recalculate getRecalculate() {
        return item.getRecalculate();
    }

    @Override
    public List<AttributeBonus> getBonuses() {
        return resolveDecoration(new ArrayList<>(item.getBonuses()), "getBonuses", AttributeBonus.class);
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
        for (ItemExtension extension : item.getExtensions()) {
            value = resolveDecoration(value, extension, functionName, valueClass);
        }
        for (ItemExtension extension : extensions.values()) {
            value = resolveDecoration(value, extension, functionName, valueClass);
        }
        return value;
    }

    <T> List<T> resolveDecoration(List<T> list, String functionName, Class<T> valueClass) {
        for (ItemExtension extension : item.getExtensions()) {
            resolveDecoration(list, extension, functionName, valueClass);
        }
        for (ItemExtension extension : extensions.values()) {
            resolveDecoration(list, extension, functionName, valueClass);
        }
        return list;
    }

    private <T> T resolveDecoration(T value, ItemExtension extension, String functionName, Class<T> valueClass) {
        try {
            Method method = extension.getClass().getMethod(functionName, valueClass);
            if(!method.isAnnotationPresent(ItemExtension.BaseItemOnly.class))
                value = valueClass.cast(method.invoke(extension, value));
        } catch (NoSuchMethodException ignored) {
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return value;
    }

    private <T> void resolveDecoration(List<T> list, ItemExtension extension, String functionName, Class<T> valueClass) {
        try {
            Method method = extension.getClass().getMethod(functionName);
            if(!method.isAnnotationPresent(ItemExtension.BaseItemOnly.class)) {
                List<?> append = (List<?>) method.invoke(extension);
                for (Object o : append) {
                    if(valueClass.isInstance(o))
                        list.add(valueClass.cast(o));
                }
            }
        } catch (NoSuchMethodException ignored) {
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
