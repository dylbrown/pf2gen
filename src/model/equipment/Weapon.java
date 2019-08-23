package model.equipment;

import model.enums.DamageType;
import model.enums.Rarity;
import model.enums.Slot;
import model.enums.WeaponProficiency;

import java.util.Collections;
import java.util.List;

public class Weapon extends Equipment {
    private final Dice damage;
    private final DamageType damageType;
    private final int hands;
    private final WeaponGroup group;
    private final List<ItemTrait> traits;
    private final WeaponProficiency proficiency;
    private final boolean uncommon;

    public Weapon(double weight, double value, String name, String description, Rarity rarity, Dice damage, DamageType damageType, int hands, WeaponGroup group, List<ItemTrait> traits, WeaponProficiency proficiency, boolean uncommon) {
        super(weight, value, name, description, rarity, getSlot(hands));
        this.damage = damage;
        this.damageType = damageType;
        this.hands = hands;
        this.group = group;
        this.traits = traits;
        this.proficiency = proficiency;
        this.uncommon = uncommon;
    }

    private static Slot getSlot(int hands) {
        return hands == 2 ? Slot.TwoHands : Slot.OneHand;
    }

    public Dice getDamage() {
        return damage;
    }

    public DamageType getDamageType() {
        return damageType;
    }

    public int getHands() {
        return hands;
    }

    public WeaponGroup getGroup() {
        return group;
    }

    public List<ItemTrait> getTraits() {
        return Collections.unmodifiableList(traits);
    }

    public WeaponProficiency getProficiency() {
        return proficiency;
    }

    public boolean isUncommon() {
        return uncommon;
    }

    @Override
    public Weapon copy() {
        return new Weapon(getWeight(),getValue(),getName(),getDescription(),getRarity(),getDamage(),getDamageType(),getHands(),getGroup(),getTraits(),getProficiency(), uncommon);
    }

    public boolean isRanged(){
        return false;
    }
}
