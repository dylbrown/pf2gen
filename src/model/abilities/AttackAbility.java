package model.abilities;

import model.AttributeMod;
import model.abilities.abilitySlots.AbilitySlot;
import model.enums.Type;
import model.equipment.Weapon;

import java.util.Collections;
import java.util.List;

public class AttackAbility extends Ability {
	private final List<Weapon> weapons;

	public AttackAbility(int level, String name, String description, List<String> prerequisites, List<AttributeMod> requiredAttrs, String customMod, List<AbilitySlot> abilitySlots, Type type, boolean multiple, List<Weapon> weapons) {
		super(level, name, description, prerequisites, requiredAttrs, customMod, abilitySlots, type, multiple);
		this.weapons = weapons;
	}

	public List<Weapon> getAttacks() {
		return Collections.unmodifiableList(weapons);
	}
}
