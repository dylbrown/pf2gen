package model.items;

import model.abilities.Ability;
import model.attributes.AttributeBonus;
import model.enums.Slot;
import model.enums.Trait;

import java.util.Collection;
import java.util.List;

public class SearchItem implements Item {
    private final String name;
    public SearchItem(String name) {
        this.name = name;
    }

    @Override
    public Item copy() {
        return new SearchItem(name);
    }

    @Override
    public List<Trait> getTraits() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<AttributeBonus> getBonuses() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Ability> getAbilities() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends ItemExtension> T getExtension(Class<T> extensionClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<ItemExtension> getExtensions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ItemExtension getExtensionByName(String extensionName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends ItemExtension> boolean hasExtension(Class<T> extensionClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasExtension(String extensionName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getWeight() {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getPrettyWeight() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Slot getSlot() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getHands() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getLevel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getCategory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getSubCategory() {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    @Override
    public int compareTo(Item o) {
        return name.compareTo(o.getName());
    }
}
