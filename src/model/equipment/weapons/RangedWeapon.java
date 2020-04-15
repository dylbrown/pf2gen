package model.equipment.weapons;

import model.equipment.Equipment;
import model.equipment.runes.runedItems.Enchantable;
import model.equipment.runes.runedItems.RunedRangedWeapon;

public class RangedWeapon extends Weapon implements Enchantable {
    private final int range;
    private final int reload;

    public RangedWeapon(RangedWeapon.Builder builder) {
        super(builder);
        this.range = builder.range;
        this.reload = builder.reload;
    }

    @Override
    public Equipment makeRuned() {
        return new RunedRangedWeapon(this);
    }

    @Override
    public boolean isRanged(){
        return true;
    }

    public int getRange() {
        return range;
    }

    public int getReload() {
        return reload;
    }

    public static class Builder extends Weapon.Builder {
        private int range = 0;
        private int reload = 0;

        public Builder(Weapon.Builder builder) {
            super(builder.build());
            this.setCategory("Ranged Weapon");
        }

        public Builder(RangedWeapon weapon) {
            super(weapon);
            range = weapon.range;
            reload = weapon.reload;
            this.setCategory("Ranged Weapon");
        }

        @Override
        public RangedWeapon build() {
            return new RangedWeapon(this);
        }

        public void setRange(int range) {
            this.range = range;
        }

        public void setReload(int reload) {
            this.reload = reload;
        }
    }
}
