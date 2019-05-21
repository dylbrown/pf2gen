package model.equipment;

import model.enums.Rarity;
import model.enums.Slot;

import java.util.List;

public class Armor extends Equipment {
    private int AC;
    private int TAC;
    private int maxDex;
    private int ACP;
    private int speedPenalty;
    private List<ItemTrait> traits;

    public Armor(double weight, double value, String name, String description, Rarity rarity, int acMod, int tacMod, int maxDex, int acp, int speedPenalty, List<ItemTrait> traits) {
        super(weight, value, name, description, rarity, Slot.Armor);
        this.AC = acMod;
        this.TAC = tacMod;
        this.maxDex = maxDex;
        this.ACP = acp;
        this.speedPenalty = speedPenalty;
        this.traits = traits;
    }

    public Armor(double weight, double value, String name, String description, Rarity rarity, int acMod, int tacMod, int maxDex, int acp, int speedPenalty, List<ItemTrait> traits, Slot weirdSlot) {
        super(weight, value, name, description, rarity, weirdSlot);
        this.AC = acMod;
        this.TAC = tacMod;
        this.maxDex = maxDex;
        this.ACP = acp;
        this.speedPenalty = speedPenalty;
        this.traits = traits;
    }

    public int getAC() {
        return AC;
    }

    public int getTAC() {
        return TAC;
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

    public List<ItemTrait> getTraits() {
        return traits;
    }

    @Override
    public Armor copy() {
        return new Armor(getWeight(),getValue(),getName(),getDescription(),getRarity(),getAC(),getTAC(),getMaxDex(),getACP(),getSpeedPenalty(),getTraits());
    }
}
