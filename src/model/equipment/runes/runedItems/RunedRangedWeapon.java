package model.equipment.runes.runedItems;

import model.equipment.runes.WeaponRune;
import model.equipment.weapons.Damage;
import model.equipment.weapons.RangedWeapon;

public class RunedRangedWeapon extends RangedWeapon implements RunedEquipment<WeaponRune> {
    private final Runes<WeaponRune> runes;
    private final RangedWeapon baseWeapon;

    public RunedRangedWeapon(RangedWeapon weapon) {
        super(new RangedWeapon.Builder(weapon));
        baseWeapon = weapon;
        runes = new Runes<>(weapon.getName(), WeaponRune.class);
    }

    @Override
    public double getValue() {
        return super.getValue() + runes.getValue();
    }

    @Override
    public String getName() {
        return runes.getFullName().get();
    }

    @Override
    public Runes<WeaponRune> getRunes() {
        return runes;
    }

    @Override
    public RangedWeapon getBaseItem() {
        return baseWeapon;
    }

    @Override
    public int getAttackBonus() {
        return runes.getAll().stream()
                .map(WeaponRune::getAttackBonus)
                .reduce(0, Integer::sum);
    }

    @Override
    public Damage getDamage() {
        return RunedWeapon.getDamageStatic(runes, baseWeapon);
    }
}
