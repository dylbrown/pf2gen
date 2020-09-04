package model.equipment.armor;

import model.equipment.BaseItem;
import model.equipment.Item;
import model.equipment.ItemExtension;

public class Shield extends ItemExtension {
    public static final Shield NO_SHIELD;

    static {
        BaseItem.Builder builder = new BaseItem.Builder();
        builder.getExtension(Armor.Builder.class);
        builder.getExtension(Shield.Builder.class);
        NO_SHIELD = builder.build().getExtension(Shield.class);
    }
    private final int hardness;
    private final int hp;
    private final int bt;

    public Shield(Shield.Builder builder, Item baseItem) {
        super(baseItem);
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

    public Shield copy() {
        return new Shield.Builder(this).build(getBaseItem());
    }

    public static class Builder extends ItemExtension.Builder {
        private int hardness;
        private int hp;
        private int bt;

        public Builder() {}

        public Builder(Shield shield) {
            this.hardness = shield.hardness;
            this.hp = shield.hp;
            this.bt = shield.bt;
        }

        @Override
        public Shield build(Item baseItem) {
            return new Shield(this, baseItem);
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
