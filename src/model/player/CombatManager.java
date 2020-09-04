package model.player;

import javafx.beans.property.ReadOnlyObjectProperty;
import model.attributes.Attribute;
import model.enums.Slot;
import model.equipment.Item;
import model.equipment.armor.Armor;
import model.equipment.armor.Shield;
import model.equipment.weapons.Damage;
import model.equipment.weapons.DamageModifier;
import model.equipment.weapons.Weapon;

import java.util.*;

import static model.ability_scores.AbilityScore.Dex;
import static model.ability_scores.AbilityScore.Str;

public class CombatManager implements PlayerState {
    private final List<Weapon> attacks = new ArrayList<>();
    private final AbilityScoreManager scores;
    private final AttributeManager attributes;
    private final InventoryManager inventory;
    private final ReadOnlyObjectProperty<Integer> level;
    private final Map<String, DamageModifier> damageModifiers = new HashMap<>();

    public CombatManager(AbilityScoreManager scores, AttributeManager attributes, InventoryManager inventory,
                         ReadOnlyObjectProperty<Integer> level) {
        this.scores = scores;
        this.attributes = attributes;
        this.inventory = inventory;
        this.level = level;
    }

    public int getAC() {
        if(inventory.getEquipped(Slot.Armor) != null) {
            Armor armor = inventory.getEquipped(Slot.Armor).stats().getExtension(Armor.class);
            if(armor != null) {
                int dexMod = scores.getMod(Dex);
                if(scores.getScore(Str) < armor.getStrength())
                    dexMod = Math.min(dexMod, armor.getMaxDex());
                return 10 + attributes.getProficiency(Attribute.valueOf(armor.getProficiency()), "")
                        .getValue().getMod(level.get()) + armor.getAC() + dexMod;
            }
        }
        return 10 + attributes.getProficiency(Attribute.Unarmored, "").getValue().getMod(level.get()) + scores.getMod(Dex);
    }

    public int getArmorProficiency() {
        if(inventory.getEquipped(Slot.Armor) != null) {
            Armor armor = inventory.getEquipped(Slot.Armor).stats().getExtension(Armor.class);
            if(armor != null)
                return attributes.getProficiency(Attribute.valueOf(armor.getProficiency()), "").getValue().getMod(level.get());
        }
        return attributes.getProficiency(Attribute.Unarmored, "").getValue().getMod(level.get());
    }

    public Item getArmor() {
        if(inventory.getEquipped(Slot.Armor) != null) {
            return inventory.getEquipped(Slot.Armor).stats();
        }
        return Armor.NO_ARMOR.getBaseItem();
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
        return Shield.NO_SHIELD.getBaseItem();
    }

    public int getAttackMod(Item weapon) {
        int mod = attributes.getProficiency(weapon.getExtension(Weapon.class).getProficiency(), weapon).getMod(level.get());

        if(weapon.getTraits().stream().anyMatch(t->t.getName().equals("Finesse")))
            return mod + weapon.getExtension(Weapon.class).getAttackBonus() + Math.max(scores.getMod(Str), scores.getMod(Dex));
        else if(weapon.getExtension(Weapon.class).isRanged())
            return mod + weapon.getExtension(Weapon.class).getAttackBonus() + scores.getMod(Dex);
        else
            return mod + weapon.getExtension(Weapon.class).getAttackBonus() + scores.getMod(Str);
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
        Weapon extension = weapon.getExtension(Weapon.class);
        Damage damage = extension.getDamage().add(getDamageMod(weapon), extension.getDamageType());
        for (DamageModifier damageModifier : damageModifiers.values()) {
            damage = damageModifier.apply(extension, damage);
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
        } else if(weapon.getHands() == 2)
            return (int) (mod * 1.5);
        else
            return mod;
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
