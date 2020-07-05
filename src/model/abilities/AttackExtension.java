package model.abilities;

import model.equipment.weapons.Weapon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AttackExtension extends AbilityExtension {
	private final List<Weapon> weapons;

	private AttackExtension(AttackExtension.Builder builder, Ability baseAbility) {
		super(baseAbility);
		this.weapons = builder.weapons;
	}

	public List<Weapon> getAttacks() {
		return Collections.unmodifiableList(weapons);
	}

	public static class Builder extends AbilityExtension.Builder {
		private List<Weapon> weapons = Collections.emptyList();

		public Builder(Builder other) {
			this.weapons = new ArrayList<>(other.weapons);
		}

		Builder() {}

		public void addWeapon(Weapon weapon) {
			if(weapons.size() == 0) weapons = new ArrayList<>();
			weapons.add(weapon);
		}

		public void setWeapons(List<Weapon> weapons) {
			this.weapons = weapons;
		}

		@Override
		public AttackExtension build(Ability baseAbility) {
			return new AttackExtension(this, baseAbility);
		}
	}
}
