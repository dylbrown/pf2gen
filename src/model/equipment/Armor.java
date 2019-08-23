package model.equipment;

import model.enums.Rarity;
import model.enums.Slot;

import java.util.List;

public class Armor extends Equipment {
    private final int AC;
    private final int maxDex;
    private final int ACP;
    private final int speedPenalty;
    private final int strength;
    private final ArmorGroup group;
    private final List<ItemTrait> traits;

    public Armor(double weight, double value, String name, String description, Rarity rarity, int acMod, int maxDex, int acp, int speedPenalty, int strength, ArmorGroup group, List<ItemTrait> traits) {
        super(weight, value, name, description, rarity, Slot.Armor);
        this.AC = acMod;
        this.maxDex = maxDex;
        this.ACP = acp;
        this.speedPenalty = speedPenalty;
        this.strength = strength;
        this.group = group;
        this.traits = traits;
    }

    Armor(double weight, double value, String name, String description, Rarity rarity, int acMod, int maxDex, int acp, int speedPenalty, int strength, ArmorGroup group, List<ItemTrait> traits, Slot weirdSlot) {
        super(weight, value, name, description, rarity, weirdSlot);
        this.AC = acMod;
        this.maxDex = maxDex;
        this.ACP = acp;
        this.speedPenalty = speedPenalty;
        this.strength = strength;
        this.group = group;
        this.traits = traits;
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

    List<ItemTrait> getTraits() {
        return traits;
    }

    @Override
    public Armor copy() {
        return new Armor(getWeight(),getValue(),getName(),getDescription(),getRarity(),getAC(),getMaxDex(),getACP(),getSpeedPenalty(),getStrength(),getGroup(), getTraits(), getSlot());
    }
}
