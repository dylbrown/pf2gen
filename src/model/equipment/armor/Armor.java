package model.equipment.armor;

import model.enums.ArmorProficiency;
import model.enums.Slot;
import model.equipment.CustomTrait;
import model.equipment.Equipment;
import model.equipment.runes.runedItems.Enchantable;
import model.equipment.runes.runedItems.RunedArmor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Armor extends Equipment implements Enchantable {
    private final int AC;
    private final int maxDex;
    private final int ACP;
    private final int speedPenalty;
    private final int strength;
    private final ArmorGroup group;
    private final List<CustomTrait> traits;
    private final ArmorProficiency proficiency;

    public Armor(Armor.Builder builder) {
        super(builder);
        this.AC = builder.AC;
        this.maxDex = builder.maxDex;
        this.ACP = builder.ACP;
        this.speedPenalty = builder.speedPenalty;
        this.strength = builder.strength;
        this.group = builder.group;
        this.traits = builder.traits;
        this.proficiency = builder.proficiency;
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

    public List<CustomTrait> getArmorTraits() {
        return Collections.unmodifiableList(traits);
    }

    public ArmorProficiency getProficiency() {
        return proficiency;
    }

    @Override
    public Armor copy() {
        return new Armor.Builder(this).build();
    }

    @Override
    public Equipment makeRuned() {
        return new RunedArmor(this);
    }

    public static class Builder extends Equipment.Builder {
        private int AC=0;
        private int maxDex=0;
        private int ACP=0;
        private int speedPenalty=0;
        private int strength=0;
        private ArmorGroup group=ArmorGroup.None;
        private List<CustomTrait> traits= new ArrayList<>();
        private ArmorProficiency proficiency = null;

        public Builder() {this.setCategory("Armor");
        this.setSlot(Slot.Armor);}

        public Builder(Armor armor) {
            super(armor);
            this.AC = armor.AC;
            this.maxDex = armor.maxDex;
            this.ACP = armor.ACP;
            this.speedPenalty = armor.speedPenalty;
            this.strength = armor.strength;
            this.group = armor.group;
            this.traits = armor.traits;
            this.proficiency = armor.proficiency;
            this.setCategory("Armor");
            this.setSubCategory(armor.getSubCategory());
        }

        @Override
        public Armor build() {
            return new Armor(this);
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

        public void setTraits(List<CustomTrait> traits) {
            this.traits = traits;
        }

        public void setProficiency(ArmorProficiency proficiency) {
            this.proficiency = proficiency;
            setSubCategory(proficiency.toString());
        }

        public void addArmorTrait(CustomTrait trait) {
            this.traits.add(trait);
        }
    }
}
