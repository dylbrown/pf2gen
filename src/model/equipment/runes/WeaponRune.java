package model.equipment.runes;

import model.equipment.Item;
import model.equipment.weapons.Damage;

public class WeaponRune extends Rune {
    private final Damage bonusDamage;
    private final int attackBonus;
    private final int bonusWeaponDice;
    private WeaponRune(Builder builder, Item baseItem) {
        super(builder, baseItem);
        bonusDamage = builder.bonusDamage;
        attackBonus = builder.bonusAttack;
        bonusWeaponDice = builder.bonusWeaponDice;
    }

    public Damage getBonusDamage() {
        return bonusDamage;
    }

    public int getAttackBonus() {
        return attackBonus;
    }

    public int getBonusWeaponDice() {
        return bonusWeaponDice;
    }

    public static class Builder extends Rune.Builder {
        private Damage bonusDamage = Damage.ZERO;
        private int bonusAttack = 0;
        private int bonusWeaponDice = 0;

        public void setBonusDamage(Damage bonusDamage) {
            this.bonusDamage = bonusDamage;
        }

        public void setAttackBonus(int bonusAttack) {
            this.bonusAttack = bonusAttack;
        }

        public void setBonusWeaponDice(int bonusWeaponDice) {
            this.bonusWeaponDice = bonusWeaponDice;
        }

        public WeaponRune build(Item baseItem) {
            return new WeaponRune(this, baseItem);
        }
    }
}
