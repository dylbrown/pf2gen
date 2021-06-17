package model.items.weapons;

import model.items.Item;


public class StrikeModifier {
    private final AttackFunction attackFunction;
    private final DamageFunction damageFunction;
    private final Item applyingItem;

    @FunctionalInterface
    public interface AttackFunction {
        Integer apply(Weapon targetWeapon, Item applyingItem, Integer attack);
    }
    @FunctionalInterface
    public interface DamageFunction {
        Damage apply(Weapon targetWeapon, Item applyingItem, Damage attack);
    }

    public StrikeModifier(AttackFunction attackFunction, DamageFunction damageFunction, Item applyingItem) {
        this.attackFunction = attackFunction;
        this.damageFunction = damageFunction;
        this.applyingItem = applyingItem;
    }

    public Integer apply(Weapon targetWeapon, Integer attack) {
        return attackFunction.apply(targetWeapon, applyingItem, attack);
    }

    public Damage apply(Weapon targetWeapon, Damage damage) {
        return damageFunction.apply(targetWeapon, applyingItem, damage);
    }
}