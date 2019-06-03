package model.equipment;

import model.enums.DamageType;
import model.enums.Rarity;
import model.enums.WeaponProficiency;

import java.util.List;

public class RangedWeapon extends Weapon {
    private final int range;
    private final int reload;

    public RangedWeapon(double weight, double value, String name, String description, Rarity rarity, Dice damage, DamageType damageType, int hands, WeaponGroup group, List<ItemTrait> traits, WeaponProficiency weaponProficiency, int range, int reload) {
        super(weight, value, name, description, rarity, damage, damageType, hands, group, traits, weaponProficiency);
        this.range = range;
        this.reload = reload;
    }

    @Override
    public boolean isRanged(){
        return true;
    }
}
