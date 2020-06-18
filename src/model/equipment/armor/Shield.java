package model.equipment.armor;

import model.enums.ArmorProficiency;
import model.enums.Slot;
import model.equipment.Equipment;
import model.equipment.runes.runedItems.Enchantable;
import model.equipment.runes.runedItems.RunedShield;

public class Shield extends Armor implements Enchantable {
    public static final Shield NO_SHIELD;

    static {
        Builder builder = new Builder();
        builder.setName("No Shield");
        NO_SHIELD = builder.build();
    }
    private final int hardness;
    private final int hp;
    private final int bt;

    public Shield(Shield.Builder builder) {
        super(builder);
        this.hardness = builder.hardness;
        this.hp = builder.hp;
        this.bt = builder.bt;
    }

    public int getHardness() {
        return hardness;
    }

    public int getHP() {
        return hp;
    }

    public int getBT() {
        return bt;
    }

    @Override
    public Shield copy() {
        return new Shield.Builder(this).build();
    }

    @Override
    public Equipment makeRuned() {
        return new RunedShield(this);
    }

    public static class Builder extends Armor.Builder {
        private int hardness;
        private int hp;
        private int bt;

        public Builder() { init(); }

        public Builder(Armor.Builder builder) {
            super(builder.build());
            init();
        }

        public Builder(Shield shield) {
            super(shield);
            this.hardness = shield.hardness;
            this.hp = shield.hp;
            this.bt = shield.bt;
        }

        private void init() {
            this.setProficiency(ArmorProficiency.Shield);
            this.setSlot(Slot.OneHand);
        }

        @Override
        public Shield build() {
            setSubCategory("Shield");
            return new Shield(this);
        }

        public void setHardness(int hardness) {
            this.hardness = hardness;
        }

        public void setHP(int hp) {
            this.hp = hp;
        }

        public void setBT(int bt) {
            this.bt = bt;
        }
    }
}
