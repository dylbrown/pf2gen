package model.items;

import model.abilities.Ability;
import model.attributes.AttributeBonus;
import model.enums.Slot;
import model.enums.Trait;

import java.util.Collection;
import java.util.List;

public class ItemFormula implements Item {
    private final Item item;
    public ItemFormula(Item item) {
        this.item = item;
    }

    @Override
    public <T extends ItemExtension> T getExtension(Class<T> extensionClass) {
        return item.getExtension(extensionClass);
    }

    @Override
    public Collection<ItemExtension> getExtensions() {
        return item.getExtensions();
    }

    @Override
    public ItemExtension getExtensionByName(String extensionName) {
        return item.getExtensionByName(extensionName);
    }

    @Override
    public <T extends ItemExtension> boolean hasExtension(Class<T> extensionClass) {
        return item.hasExtension(extensionClass);
    }

    @Override
    public boolean hasExtension(String extensionName) {
        return item.hasExtension(extensionName);
    }

    @Override
    public double getWeight() {
        return 0;
    }

    @Override
    public double getValue() {
        switch (item.getLevel()) {
            case 0: return 5;
            case 1: return 10;
            case 2: return 20;
            case 3: return 30;
            case 4: return 50;
            case 5: return 80;
            case 6: return 130;
            case 7: return 180;
            case 8: return 250;
            case 9: return 350;
            case 10: return 500;
            case 11: return 700;
            case 12: return 1000;
            case 13: return 1500;
            case 14: return 2250;
            case 15: return 3250;
            case 16: return 5000;
            case 17: return 7500;
            case 18: return 12000;
            case 19: return 20000;
            case 20: return 35000;
        }
        return 0;
    }

    @Override
    public String getPrettyWeight() {
        return "0";
    }

    @Override
    public Slot getSlot() {
        return item.getSlot();
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
        return item.getName();
    }

    @Override
    public String getRawName() {
        return item.getRawName();
    }

    @Override
    public String getDescription() {
        return item.getDescription();
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
}
