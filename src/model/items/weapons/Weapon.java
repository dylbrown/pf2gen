package model.items.weapons;

import model.enums.Slot;
import model.enums.Trait;
import model.enums.WeaponProficiency;
import model.items.BaseItem;
import model.items.Item;
import model.items.ItemExtension;
import model.items.runes.runedItems.Enchantable;

import java.util.ArrayList;
import java.util.List;

public class Weapon extends ItemExtension {
    private final Dice damageDice;
    private final Damage damage;
    private final DamageType damageType;
    private final WeaponGroup group;
    private final WeaponProficiency proficiency;

    private Weapon(Builder builder, Item baseItem) {
        super(baseItem);
        this.damageDice = builder.damageDice;
        this.damageType = builder.damageType;
        this.group = builder.group;
        this.proficiency = builder.proficiency;
        damage = new Damage.Builder()
                    .addDice(damageDice)
                    .setDamageType(damageType)
                    .build();
    }

    @ItemDecorator
    public String getSubCategory(String subCategory) {
        return proficiency.name();
    }

    @ItemDecorator
    public Slot getSlot(Slot slot) {
        return getItem().getHands() == 2 ? Slot.TwoHands : Slot.OneHand;
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

    public WeaponProficiency getProficiency() {
        return proficiency;
    }

    public boolean isRanged(){
        return getItem().hasExtension(RangedWeapon.class);
    }

    public static class Builder extends ItemExtension.Builder {
        private Dice damageDice;
        private DamageType damageType;
        private WeaponGroup group;
        private List<Trait> traits = new ArrayList<>();
        private WeaponProficiency proficiency;

        public Builder(BaseItem.Builder builder) {
            builder.getExtension(Enchantable.Builder.class).weaponRunes = true;
        }

        @Override
        public Weapon build(Item baseItem) {
            return new Weapon(this, baseItem);
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
        }

        public void addWeaponTrait(Trait trait) {
            traits.add(trait);
        }
    }
}
