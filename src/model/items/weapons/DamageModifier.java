package model.items.weapons;

@FunctionalInterface
public interface DamageModifier {
    Damage apply(Weapon w, Damage d);
}
