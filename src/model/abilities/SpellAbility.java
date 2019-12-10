package model.abilities;

import model.spells.CasterType;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class SpellAbility extends Ability {
	private final Map<Integer, Integer> spellSlots;
	private final CasterType casterType;
	private SpellAbility(Builder builder) {
		super(builder);
		spellSlots = builder.spellSlots;
		casterType = builder.casterType;
	}

	public Map<Integer, Integer> getSpellSlots() {
		return Collections.unmodifiableMap(spellSlots);
	}

	public CasterType getCasterType() {
		return casterType;
	}

	public static class Builder extends Ability.Builder {
		private Map<Integer, Integer> spellSlots = Collections.emptyMap();
		private CasterType casterType = CasterType.None;
		public Builder() {}
		public Builder(Ability.Builder builder) {
			super(builder);
		}

		public void addSpellSlots(int level, int count) {
			if(spellSlots.size() == 0) spellSlots = new TreeMap<>();
			spellSlots.put(level, spellSlots.getOrDefault(level, 0) + count);
		}

		public void setCasterType(CasterType casterType) {
			this.casterType = casterType;
		}

		@Override
		public SpellAbility build() {
			return new SpellAbility(this);
		}
	}
}
