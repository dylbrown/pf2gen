package model.equipment.weapons;

import model.enums.Slot;
import model.enums.WeaponProficiency;
import model.equipment.CustomTrait;
import model.equipment.Equipment;
import model.equipment.runes.runedItems.Enchantable;
import model.equipment.runes.runedItems.RunedWeapon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Weapon extends Equipment implements Enchantable {
    private final Dice damageDice;
    private final Damage damage;
    private final DamageType damageType;
    private final WeaponGroup group;
    private final List<CustomTrait> traits;
    private final WeaponProficiency proficiency;
    private final boolean uncommon;

    public Weapon(Weapon.Builder builder) {
        super(builder);
        this.damageDice = builder.damageDice;
        this.damageType = builder.damageType;
        this.group = builder.group;
        this.traits = builder.traits;
        this.proficiency = builder.proficiency;
        this.uncommon = builder.uncommon;
        damage = new Damage.Builder()
                    .addDice(damageDice)
                    .setDamageType(damageType)
                    .build();
    }

    @Override
    public Slot getSlot() {
        return getHands() == 2 ? Slot.TwoHands : Slot.OneHand;
    }

    public Dice getDamageDice() {
        return damageDice;
    }

    public int getAttackBonus() {return 0;}

    public Damage getDamage() {return damage;}

    public DamageType getDamageType() {
        return damageType;
    }

    public WeaponGroup getGroup() {
        return group;
    }

    public List<CustomTrait> getWeaponTraits() {
        return Collections.unmodifiableList(traits);
    }

    public WeaponProficiency getProficiency() {
        return proficiency;
    }

    public boolean isUncommon() {
        return uncommon;
    }

    @Override
    public Weapon copy() {
        return new Weapon.Builder(this).build();
    }

    public boolean isRanged(){
        return false;
    }

    @Override
    public Equipment makeRuned() {
        return new RunedWeapon(this);
    }

    public static class Builder extends Equipment.Builder {
        private Dice damageDice;
        private DamageType damageType;
        private WeaponGroup group;
        private List<CustomTrait> traits = new ArrayList<>();
        private WeaponProficiency proficiency;
        private boolean uncommon;

        public Builder() { this.setCategory("Weapon");}

        public Builder(Weapon weapon) {
            super(weapon);
            this.damageDice = weapon.damageDice;
            this.damageType = weapon.damageType;
            this.group = weapon.group;
            this.traits = new ArrayList<>(weapon.traits);
            this.proficiency = weapon.proficiency;
            this.uncommon = weapon.uncommon;
            this.setCategory("Weapon");
            this.setSubCategory(weapon.getSubCategory());
        }

        @Override
        public Weapon build() {
            return new Weapon(this);
        }

        public void setDamageDice(Dice damageDice) {
            this.damageDice = damageDice;
        }

        public void setDamageType(DamageType damageType) {
            this.damageType = damageType;
        }

        public void setGroup(WeaponGroup group) {
            this.group = group;
        }

        public void setProficiency(WeaponProficiency proficiency) {
            this.proficiency = proficiency;
            this.setSubCategory(proficiency.toString());
        }

        public void setUncommon(boolean uncommon) {
            this.uncommon = uncommon;
        }

        public void addWeaponTrait(CustomTrait customTrait) {
            traits.add(customTrait);
        }
    }
}
