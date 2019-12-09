package model.abilities;

import model.equipment.Weapon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AttackAbility extends Ability {
	private final List<Weapon> weapons;

	private AttackAbility(AttackAbility.Builder builder) {
		super(builder);
		this.weapons = builder.weapons;
	}

	public List<Weapon> getAttacks() {
		return Collections.unmodifiableList(weapons);
	}

	public static class Builder extends Ability.Builder {
		private List<Weapon> weapons = Collections.emptyList();

		public Builder(Ability.Builder builder) {
			super(builder);
		}

		public void addWeapon(Weapon weapon) {
			if(weapons.size() == 0) weapons = new ArrayList<>();
			weapons.add(weapon);
		}

		public void setWeapons(List<Weapon> weapons) {
			this.weapons = weapons;
		}

		@Override
		public AttackAbility build() {
			return new AttackAbility(this);
		}
	}
}
