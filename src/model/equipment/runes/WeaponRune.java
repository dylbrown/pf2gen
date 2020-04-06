package model.equipment.runes;

import model.equipment.weapons.Damage;

public class WeaponRune extends Rune {
    private final Damage bonusDamage;
    private final int attackBonus;
    private WeaponRune(Builder builder) {
        super(builder);
        bonusDamage = builder.bonusDamage;
        attackBonus = builder.bonusAttack;
    }

    public Damage getBonusDamage() {
        return bonusDamage;
    }

    public int getAttackBonus() {
        return attackBonus;
    }

    public static class Builder extends Rune.Builder {
        private Damage bonusDamage = Damage.ZERO;
        private int bonusAttack = 0;

        public void setBonusDamage(Damage bonusDamage) {
            this.bonusDamage = bonusDamage;
        }

        public void setAttackBonus(int bonusAttack) {
            this.bonusAttack = bonusAttack;
        }

        public WeaponRune build() {
            return new WeaponRune(this);
        }
    }
}
