package model.player;

import javafx.beans.property.ReadOnlyObjectProperty;
import model.abilities.Ability;
import model.abilities.AttackExtension;
import model.attributes.BaseAttribute;
import model.enums.Slot;
import model.items.Item;
import model.items.armor.Armor;
import model.items.armor.Shield;
import model.items.runes.runedItems.RunedWeapon;
import model.items.weapons.*;

import java.util.*;

import static model.ability_scores.AbilityScore.Dex;
import static model.ability_scores.AbilityScore.Str;

public class CombatManager implements PlayerState {
    private final List<Weapon> attacks = new ArrayList<>();
    private final AbilityScoreManager scores;
    private final AttributeManager attributes;
    private final InventoryManager inventory;
    private final ReadOnlyObjectProperty<Integer> level;
    private final Map<String, StrikeModifier> strikeModifiers = new HashMap<>();
    private final Map<String, DamageModifier> damageModifiers = new HashMap<>();

    public CombatManager(AbilityScoreManager scores, AttributeManager attributes, InventoryManager inventory,
                         ReadOnlyObjectProperty<Integer> level, Applier<Ability> abilityApplier) {
        this.scores = scores;
        this.attributes = attributes;
        this.inventory = inventory;
        this.level = level;
        abilityApplier.onApply(ability -> {
            AttackExtension attackExt = ability.getExtension(AttackExtension.class);
            if(attackExt != null) {
                addAttacks(attackExt.getAttacks());
            }
        });
        abilityApplier.onRemove(ability -> {
            AttackExtension attackExt = ability.getExtension(AttackExtension.class);
            if(attackExt != null) {
                removeAttacks(attackExt.getAttacks());
            }
        });
    }

    public int getAC() {
        if(inventory.getEquipped(Slot.Armor) != null) {
            Armor armor = inventory.getEquipped(Slot.Armor).stats().getExtension(Armor.class);
            if(armor != null) {
                int dexMod = scores.getMod(Dex);
                dexMod = Math.min(dexMod, armor.getMaxDex());
                return 10 + attributes.getProficiency(armor.getProficiency().toAttribute())
                        .getValue().getMod(level.get()) + armor.getAC() + dexMod;
            }
        }
        return 10 + attributes.getProficiency(BaseAttribute.Unarmored).getValue().getMod(level.get()) + scores.getMod(Dex);
    }

    public int getArmorProficiency() {
        if(inventory.getEquipped(Slot.Armor) != null) {
            Armor armor = inventory.getEquipped(Slot.Armor).stats().getExtension(Armor.class);
            if(armor != null)
                return attributes.getProficiency(armor.getProficiency().toAttribute()).getValue().getMod(level.get());
        }
        return attributes.getProficiency(BaseAttribute.Unarmored).getValue().getMod(level.get());
    }

    public Item getArmor() {
        if(inventory.getEquipped(Slot.Armor) != null) {
            return inventory.getEquipped(Slot.Armor).stats();
        }
        return Armor.NO_ARMOR.getItem();
    }

    public Item getShield() {
        if(inventory.getEquipped(Slot.OneHand) != null) {
            Item stats = inventory.getEquipped(Slot.OneHand).stats();
            if(stats.hasExtension(Shield.class)) return stats;
        }
        if(inventory.getEquipped(Slot.OffHand) != null) {
            Item stats = inventory.getEquipped(Slot.OffHand).stats();
            if(stats.hasExtension(Shield.class)) return stats;
        }
        return Shield.NO_SHIELD.getItem();
    }

    public int getAttackMod(Item weapon) {
        int mod = attributes.getProficiency(weapon.getExtension(Weapon.class).getProficiency(), weapon).getMod(level.get());

        RunedWeapon extension = weapon.getExtension(RunedWeapon.class);
        if(extension != null) {
            mod += extension.getAttackBonus();
        }

        if(weapon.getTraits().stream().anyMatch(t->t.getName().equals("Finesse")))
            mod += weapon.getExtension(Weapon.class).getAttackBonus() + Math.max(scores.getMod(Str), scores.getMod(Dex));
        else if(weapon.hasExtension(RangedWeapon.class))
            mod += weapon.getExtension(Weapon.class).getAttackBonus() + scores.getMod(Dex);
        else
            mod += weapon.getExtension(Weapon.class).getAttackBonus() + scores.getMod(Str);

        for (StrikeModifier strikeModifier : strikeModifiers.values()) {
            mod = strikeModifier.apply(weapon.getExtension(Weapon.class), mod);
        }
        return mod;
    }

    void addAttacks(List<Weapon> attacks) {
        this.attacks.addAll(attacks);
    }

    void removeAttacks(List<Weapon> attacks) {
        this.attacks.removeAll(attacks);
    }

    public List<Weapon> getAttacks() {
        return Collections.unmodifiableList(attacks);
    }

    public Damage getDamage(Item weapon) {
        RunedWeapon runedWeapon = weapon.getExtension(RunedWeapon.class);
        Weapon extension = weapon.getExtension(Weapon.class);
        Damage damage;
        if(runedWeapon != null) {
            damage = runedWeapon.getDamage().add(getDamageMod(weapon), extension.getDamageType());
        }else{
            damage = extension.getDamage().add(getDamageMod(weapon), extension.getDamageType());
        }
        for (DamageModifier damageModifier : damageModifiers.values()) {
            damage = damageModifier.apply(extension, damage);
        }
        for (StrikeModifier strikeModifier : strikeModifiers.values()) {
            damage = strikeModifier.apply(extension, damage);
        }
        return damage;
    }

    private int getDamageMod(Item weapon) {
        Integer mod = scores.getMod(Str);
        if(weapon.getTraits().stream().anyMatch(t->t.getName().equals("Thrown")))
            return mod;
        else if(weapon.getExtension(Weapon.class).isRanged()){
            if(weapon.getTraits().stream().anyMatch(t->t.getName().equals("Propulsive"))) {
                return (mod > 0) ? mod / 2 : mod;
            } else return 0;
        } else return mod;
    }

    void addStrikeModifier(String name, StrikeModifier d) {
        strikeModifiers.put(name, d);
    }

    void removeStrikeModifier(String name) {
        strikeModifiers.remove(name);
    }

    void addDamageModifier(String name, DamageModifier d) {
        damageModifiers.put(name, d);
    }

    void removeDamageModifier(String name) {
        damageModifiers.remove(name);
    }

    @Override
    public void reset(PC.ResetEvent resetEvent) {

    }
}
