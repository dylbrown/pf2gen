package model.equipment;

import model.enums.ArmorProficiency;
import model.enums.Rarity;
import model.enums.Slot;

import java.util.List;

public class Shield extends Armor {
    private final int hardness;
    private final int hp;
    private final int bt;

    public Shield(double weight, double value, String name, String description, Rarity rarity, int acMod, int maxDex, int speedPenalty, int hardness, int hp, int bt, List<ItemTrait> traits) {
        super(weight, value, name, description, rarity, acMod, maxDex, 0, speedPenalty, 0, ArmorGroup.None, traits, ArmorProficiency.Shield, Slot.OneHand);
        this.hardness = hardness;
        this.hp = hp;
        this.bt = bt;
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
        return new Shield(getWeight(),getValue(),getName(),getDescription(),getRarity(),getAC(),getMaxDex(),getSpeedPenalty(), getHardness(), getHP(), getBT(), getTraits());
    }
}
