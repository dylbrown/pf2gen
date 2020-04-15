package model.equipment.runes.runedItems;

import model.equipment.runes.WeaponRune;
import model.equipment.weapons.Damage;
import model.equipment.weapons.Dice;
import model.equipment.weapons.MultiDamage;
import model.equipment.weapons.Weapon;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RunedWeapon extends Weapon implements RunedEquipment<WeaponRune> {
    private final Runes<WeaponRune> runes;
    private final Weapon baseWeapon;

    public RunedWeapon(Weapon weapon) {
        super(new Weapon.Builder(weapon));
        runes = new Runes<>(weapon.getName(), WeaponRune.class);
        baseWeapon = weapon;
    }

    @Override
    public Weapon getBaseItem() {
        return baseWeapon;
    }

    @Override
    public String getName() {
        return runes.getFullName().get();
    }

    @Override
    public double getValue() {
        return super.getValue() + runes.getValue();
    }

    @Override
    public Runes<WeaponRune> getRunes() {
        return runes;
    }


    @Override
    public int getAttackBonus() {
        return runes.getAll().stream()
                .map(WeaponRune::getAttackBonus)
                .reduce(0, Integer::sum);
    }

    @Override
    public Damage getDamage() {
        return getDamageStatic(runes, baseWeapon);
    }

    static Damage getDamageStatic(Runes<WeaponRune> runes, Weapon baseWeapon) {
        Stream<Damage> damages = runes.getAll().stream()
                .map(WeaponRune::getBonusDamage);

        // Add extra weapon damage dice
        Integer bonusDice = runes.getAll().stream()
                .map(WeaponRune::getBonusWeaponDice)
                .reduce(0, Integer::sum);
        Dice strikingDice = Dice.get(bonusDice, baseWeapon.getDamageDice().getSize());
        Damage striking = new Damage.Builder()
                .addDice(strikingDice)
                .setDamageType(baseWeapon.getDamageType())
                .build();

        return new MultiDamage(baseWeapon.getDamage(),
                Stream.concat(damages, Stream.of(striking))
                        .filter(d->!d.equals(Damage.ZERO))
                        .collect(Collectors.toList()));
    }
}
