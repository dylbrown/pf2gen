package model.equipment;

import model.enums.DamageType;
import model.enums.Rarity;
import model.enums.Slot;
import model.enums.WeaponProficiency;

import java.util.Collections;
import java.util.List;

public class Weapon extends Equipment {
    private Dice damage;
    private DamageType damageType;
    private int hands;
    private WeaponGroup group;
    private List<ItemTrait> traits;
    private WeaponProficiency proficiency;

    public Weapon(double weight, double value, String name, String description, Rarity rarity, Dice damage, DamageType damageType, int hands, WeaponGroup group, List<ItemTrait> traits, WeaponProficiency proficiency) {
        super(weight, value, name, description, rarity, getSlot(hands));
        this.damage = damage;
        this.damageType = damageType;
        this.hands = hands;
        this.group = group;
        this.traits = traits;
        this.proficiency = proficiency;
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

    @Override
    public Weapon copy() {
        return new Weapon(getWeight(),getValue(),getName(),getDescription(),getRarity(),getDamage(),getDamageType(),getHands(),getGroup(),getTraits(),getProficiency());
    }
}
