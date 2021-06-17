package model.items.runes.runedItems;

import model.items.Item;
import model.items.ItemExtension;

public class Enchantable extends ItemExtension {
    private final boolean armorRunes, weaponRunes;

    protected Enchantable(Item item, Builder builder) {
        super(item);
        this.armorRunes = builder.armorRunes;
        this.weaponRunes = builder.weaponRunes;
    }

    public boolean canHaveArmorRunes() {
        return armorRunes;
    }

    public boolean canHaveWeaponRunes() {
        return weaponRunes;
    }

    public boolean isEnchantable() {
        return armorRunes || weaponRunes;
    }

    public static class Builder extends ItemExtension.Builder {
        public boolean armorRunes = false;
        public boolean weaponRunes = false;

        @Override
        public ItemExtension build(Item baseItem) {
            return new Enchantable(baseItem, this);
        }
    }
}
