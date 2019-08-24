package model.equipment;

import model.enums.ArmorProficiency;
import model.enums.Rarity;
import model.enums.Slot;

import java.util.Collections;
import java.util.List;

public class Armor extends Equipment {
    private final int AC;
    private final int maxDex;
    private final int ACP;
    private final int speedPenalty;
    private final int strength;
    private final ArmorGroup group;
    private final List<ItemTrait> traits;
    private final ArmorProficiency proficiency;

    public Armor(double weight, double value, String name, String description, Rarity rarity, int acMod, int maxDex, int acp, int speedPenalty, int strength, ArmorGroup group, List<ItemTrait> traits, ArmorProficiency proficiency) {
        super(weight, value, name, description, rarity, Slot.Armor);
        this.AC = acMod;
        this.maxDex = maxDex;
        this.ACP = acp;
        this.speedPenalty = speedPenalty;
        this.strength = strength;
        this.group = group;
        this.traits = traits;
        this.proficiency = proficiency;
    }

    Armor(double weight, double value, String name, String description, Rarity rarity, int acMod, int maxDex, int acp, int speedPenalty, int strength, ArmorGroup group, List<ItemTrait> traits, ArmorProficiency proficiency, Slot weirdSlot) {
        super(weight, value, name, description, rarity, weirdSlot);
        this.AC = acMod;
        this.maxDex = maxDex;
        this.ACP = acp;
        this.speedPenalty = speedPenalty;
        this.strength = strength;
        this.group = group;
        this.traits = traits;
        this.proficiency = proficiency;
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

    public List<ItemTrait> getTraits() {
        return Collections.unmodifiableList(traits);
    }

    public ArmorProficiency getProficiency() {
        return proficiency;
    }

    @Override
    public Armor copy() {
        return new Armor(getWeight(),getValue(),getName(),getDescription(),getRarity(),getAC(),getMaxDex(),getACP(),getSpeedPenalty(),getStrength(),getGroup(), getTraits(), getProficiency(), getSlot());
    }
}
