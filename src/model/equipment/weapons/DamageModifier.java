package model.equipment.weapons;

@FunctionalInterface
public interface DamageModifier {
    Damage apply(Weapon w, Damage d);
}
