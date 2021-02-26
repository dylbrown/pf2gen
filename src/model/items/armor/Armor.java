package model.items.armor;

import model.enums.ArmorProficiency;
import model.enums.Slot;
import model.items.BaseItem;
import model.items.Item;
import model.items.ItemExtension;

public class Armor extends ItemExtension {
    private final int AC;
    private final int maxDex;
    private final int ACP;
    private final int speedPenalty;
    private final int strength;
    private final ArmorGroup group;
    private final ArmorProficiency proficiency;
    public static final Armor NO_ARMOR;

    static{
        BaseItem.Builder builder = new BaseItem.Builder();
        builder.getExtension(Armor.Builder.class)
                .setProficiency(ArmorProficiency.Unarmored);
        NO_ARMOR = builder.build().getExtension(Armor.class);
    }

    private Armor(Armor.Builder builder, Item baseItem) {
        super(baseItem);
        this.AC = builder.AC;
        this.maxDex = builder.maxDex;
        this.ACP = builder.ACP;
        this.speedPenalty = builder.speedPenalty;
        this.strength = builder.strength;
        this.group = builder.group;
        this.proficiency = builder.proficiency;
    }

    @ItemDecorator
    public Slot getSlot(Slot slot) {
        return (getItem().hasExtension(Shield.class)) ? Slot.OneHand : Slot.Armor;
    }

    public int getAC() {
        return AC;
    }

    public int getMaxDex() {
        return maxDex;
    }

    public int getACP() {
        return ACP;
    }

    public int getSpeedPenalty() {
        return speedPenalty;
    }

    public int getStrength() {
        return strength;
    }

    public ArmorGroup getGroup() {
        return group;
    }

    public ArmorProficiency getProficiency() {
        return proficiency;
    }

    public Armor copy() {
        return new Armor.Builder(this).build(getItem());
    }

    public static class Builder extends ItemExtension.Builder {
        private int AC=0;
        private int maxDex=0;
        private int ACP=0;
        private int speedPenalty=0;
        private int strength=0;
        private ArmorGroup group=ArmorGroup.None;
        private ArmorProficiency proficiency = null;

        public Builder() {}

        public Builder(Armor armor) {
            this.AC = armor.AC;
            this.maxDex = armor.maxDex;
            this.ACP = armor.ACP;
            this.speedPenalty = armor.speedPenalty;
            this.strength = armor.strength;
            this.group = armor.group;
            this.proficiency = armor.proficiency;
        }

        @Override
        public Armor build(Item baseItem) {
            return new Armor(this, baseItem);
        }

        public void setAC(int AC) {
            this.AC = AC;
        }

        public void setMaxDex(int maxDex) {
            this.maxDex = maxDex;
        }

        public void setACP(int ACP) {
            this.ACP = ACP;
        }

        public void setSpeedPenalty(int speedPenalty) {
            this.speedPenalty = speedPenalty;
        }

        public void setStrength(int strength) {
            this.strength = strength;
        }

        public void setGroup(ArmorGroup group) {
            this.group = group;
        }

        public void setProficiency(ArmorProficiency proficiency) {
            this.proficiency = proficiency;
        }
    }
}
