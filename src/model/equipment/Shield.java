package model.equipment;

import model.enums.Rarity;
import model.enums.Slot;

import java.util.List;

public class Shield extends Armor {
    public Shield(double weight, double value, String name, String description, Rarity rarity, int acMod, int tacMod, int maxDex, int acp, int speedPenalty, List<ItemTrait> traits) {
        super(weight, value, name, description, rarity, acMod, tacMod, maxDex, acp, speedPenalty, traits, Slot.OneHand);
    }

    @Override
    public Shield copy() {
        return new Shield(getWeight(),getValue(),getName(),getDescription(),getRarity(),getAC(),getTAC(),getMaxDex(),getACP(),getSpeedPenalty(),getTraits());
    }
}
