package model.items.weapons;

import model.items.Item;
import model.items.ItemExtension;

public class RangedWeapon extends ItemExtension {
    private final int range;
    private final int reload;

    public RangedWeapon(Builder builder, Item baseItem) {
        super(baseItem);
        this.range = builder.range;
        this.reload = builder.reload;
    }

    public int getRange() {
        return range;
    }

    public int getReload() {
        return reload;
    }

    public static class Builder extends ItemExtension.Builder {
        private int range = 0;
        private int reload = 0;

        public Builder() {}

        public Builder(RangedWeapon weapon) {
            range = weapon.range;
            reload = weapon.reload;
        }

        @Override
        public RangedWeapon build(Item baseItem) {
            return new RangedWeapon(this, baseItem);
        }

        public void setRange(int range) {
            this.range = range;
        }

        public void setReload(int reload) {
            this.reload = reload;
        }
    }
}
