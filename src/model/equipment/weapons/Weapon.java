package model.equipment.weapons;

import model.enums.DamageType;
import model.enums.Slot;
import model.enums.WeaponProficiency;
import model.equipment.CustomTrait;
import model.equipment.Equipment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Weapon extends Equipment {
    private final Dice damage;
    private final DamageType damageType;
    private final WeaponGroup group;
    private final List<CustomTrait> traits;
    private final WeaponProficiency proficiency;
    private final boolean uncommon;

    public Weapon(Weapon.Builder builder) {
        super(builder);
        this.damage = builder.damage;
        this.damageType = builder.damageType;
        this.group = builder.group;
        this.traits = builder.traits;
        this.proficiency = builder.proficiency;
        this.uncommon = builder.uncommon;
    }

    private static Slot getSlot(int hands) {
        return hands == 2 ? Slot.TwoHands : Slot.OneHand;
    }

    public Dice getDamage() {
        return damage;
    }

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

    public static class Builder extends Equipment.Builder {
        private Dice damage;
        private DamageType damageType;
        private int hands;
        private WeaponGroup group;
        private List<CustomTrait> traits = new ArrayList<>();
        private WeaponProficiency proficiency;
        private boolean uncommon;

        public Builder() { this.setCategory("Weapon");}

        public Builder(Weapon weapon) {
            super(weapon);
            this.damage = weapon.damage;
            this.damageType = weapon.damageType;
            this.group = weapon.group;
            this.traits = new ArrayList<>(weapon.traits);
            this.proficiency = weapon.proficiency;
            this.uncommon = weapon.uncommon;
            this.setCategory("Weapon");
        }

        @Override
        public Weapon build() {
            return new Weapon(this);
        }

        public void setDamage(Dice damage) {
            this.damage = damage;
        }

        public void setDamageType(DamageType damageType) {
            this.damageType = damageType;
        }

        public void setGroup(WeaponGroup group) {
            this.group = group;
        }

        public void setProficiency(WeaponProficiency proficiency) {
            this.proficiency = proficiency;
        }

        public void setUncommon(boolean uncommon) {
            this.uncommon = uncommon;
        }

        public void addWeaponTrait(CustomTrait customTrait) {
            traits.add(customTrait);
        }
    }
}
