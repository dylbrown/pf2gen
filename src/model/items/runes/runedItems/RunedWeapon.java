package model.items.runes.runedItems;

import model.items.Item;
import model.items.ItemExtension;
import model.items.runes.WeaponRune;
import model.items.weapons.Damage;
import model.items.weapons.Dice;
import model.items.weapons.MultiDamage;
import model.items.weapons.Weapon;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RunedWeapon extends ItemExtension {
    private final Runes<WeaponRune> runes;

    public RunedWeapon(Item item) {
        super(item);
        runes = new Runes<>(item.getName(), WeaponRune.class);
        if(!item.hasExtension(Weapon.class))
            throw new RuntimeException("RunedWeapon is not Weapon");
    }

    @ItemDecorator
    public String getName(String baseName) {
        return runes.getFullName().get();
    }

    @ItemDecorator
    public double getValue(double baseValue) {
        return getItem().getValue() + runes.getValue();
    }

    public Runes<WeaponRune> getRunes() {
        return runes;
    }

    public int getAttackBonus() {
        return runes.getAll().stream()
                .map(WeaponRune::getAttackBonus)
                .reduce(0, Integer::sum);
    }

    public Damage getDamage() {
        return getDamageStatic(runes, getItem().getExtension(Weapon.class));
    }

    static Damage getDamageStatic(Runes<WeaponRune> runes, Weapon baseWeapon) {
        Stream<Damage> damages = runes.getAll().stream()
                .map(WeaponRune::getBonusDamage);

        // Add extra weapon damage dice
        Integer bonusDice = runes.getAll().stream()
                .map(WeaponRune::getBonusWeaponDice)
                .reduce(1, Integer::max);
        Dice strikingDice = Dice.get(bonusDice-1, baseWeapon.getDamageDice().getSize());
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
