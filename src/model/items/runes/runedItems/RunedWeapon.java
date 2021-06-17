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
        if(!getItem().hasExtension(Weapon.class))
            throw new RuntimeException("RunedWeapon is not Weapon");
        return getDamage(getItem().getExtension(Weapon.class),
                getItem().getExtension(Weapon.class).getDamage());
    }

    public Damage getDamage(Weapon baseWeapon, Damage baseDamage) {
        Stream<Damage> damages = runes.getAll().stream()
                .map(WeaponRune::getBonusDamage);

        // Add extra weapon damage dice
        Integer bonusDice = runes.getAll().stream()
                .map(WeaponRune::getBonusWeaponDice)
                .reduce(1, Integer::max);
        // ASSUMPTION: First damage should always be the weapon damage
        Dice strikingDice = Dice.get(bonusDice-1, baseDamage.getDice().get(0).getSize());
        Damage striking = new Damage.Builder()
                .addDice(strikingDice)
                .setDamageType(baseWeapon.getDamageType())
                .build();

        return new MultiDamage(baseDamage,
                Stream.concat(damages, Stream.of(striking))
                        .filter(d->!d.equals(Damage.ZERO))
                        .collect(Collectors.toList()));
    }
}
