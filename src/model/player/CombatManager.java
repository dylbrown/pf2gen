package model.player;

import javafx.beans.property.ReadOnlyObjectProperty;
import model.attributes.Attribute;
import model.enums.Slot;
import model.equipment.CustomTrait;
import model.equipment.Equipment;
import model.equipment.armor.Armor;
import model.equipment.armor.Shield;
import model.equipment.weapons.Damage;
import model.equipment.weapons.DamageModifier;
import model.equipment.weapons.RangedWeapon;
import model.equipment.weapons.Weapon;
import ui.Main;

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
            Armor armor = (Armor) inventory.getEquipped(Slot.Armor).stats();
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
            Armor armor = (Armor) inventory.getEquipped(Slot.Armor).stats();
            if(armor != null)
                return attributes.getProficiency(Attribute.valueOf(armor.getProficiency()), "").getValue().getMod(level.get());
        }
        return attributes.getProficiency(Attribute.Unarmored, "").getValue().getMod(level.get());
    }

    public Armor getArmor() {
        if(inventory.getEquipped(Slot.Armor) != null) {
            return (Armor) inventory.getEquipped(Slot.Armor).stats();
        }
        return Armor.NO_ARMOR;
    }

    public Shield getShield() {
        if(inventory.getEquipped(Slot.OneHand) != null) {
            Equipment stats = inventory.getEquipped(Slot.OneHand).stats();
            if(stats instanceof Shield) return (Shield) stats;
        }
        if(inventory.getEquipped(Slot.OffHand) != null) {
            Equipment stats = inventory.getEquipped(Slot.OffHand).stats();
            if(stats instanceof Shield) return (Shield) stats;
        }
        return Shield.NO_SHIELD;
    }

    public int getAttackMod(Weapon weapon) {
        int mod = attributes.getProficiency(weapon.getProficiency(), weapon).getMod(level.get());

        if(weapon.getWeaponTraits().contains(new CustomTrait("Finesse")))
            return mod + weapon.getAttackBonus() + Math.max(scores.getMod(Str), scores.getMod(Dex));
        else if(weapon instanceof RangedWeapon)
            return mod + weapon.getAttackBonus() + scores.getMod(Dex);
        else
            return mod + weapon.getAttackBonus() + scores.getMod(Str);
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

    public Damage getDamage(Weapon weapon) {
        Damage damage = weapon.getDamage().add(Main.character.combat().getDamageMod(weapon), weapon.getDamageType());
        for (DamageModifier damageModifier : damageModifiers.values()) {
            damage = damageModifier.apply(weapon, damage);
        }

        return damage;
    }

    private int getDamageMod(Weapon weapon) {
        if(weapon.getWeaponTraits().contains(new CustomTrait("Thrown")))
            return scores.getMod(Str);
        else if(weapon instanceof RangedWeapon)
            return 0;
        else if(weapon.getHands() == 2)
            return (int) (scores.getMod(Str) * 1.5);
        else
            return scores.getMod(Str);
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
