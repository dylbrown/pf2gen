package model.items;

import model.abilities.Ability;
import model.attributes.AttributeBonus;
import model.enums.Rarity;
import model.enums.Slot;
import model.enums.Trait;

import java.util.Collection;
import java.util.List;

public interface Item extends Comparable<Item> {
    <T extends ItemExtension> T getExtension(Class<T> extensionClass);
    Collection<ItemExtension> getExtensions();
    ItemExtension getExtensionByName(String extensionName);
    <T extends ItemExtension> boolean hasExtension(Class<T> extensionClass);
    boolean hasExtension(String extensionName);
    double getWeight();
    double getValue();
    Rarity getRarity();
    String getPrettyWeight();
    Slot getSlot();
    int getHands();
    int getLevel();
    String getCategory();
    String getSubCategory();
    String getName();
    String getRawName();
    String getDescription();
    Item copy();
    List<Trait> getTraits();
    List<AttributeBonus> getBonuses();
    List<Ability> getAbilities();
}
