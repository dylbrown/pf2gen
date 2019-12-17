package model.abilities;

import model.spells.CasterType;
import model.spells.Spell;

import java.util.*;

public class SpellAbility extends Ability {
	private final Map<Integer, Integer> spellSlots;
	private final Map<Integer, Integer> extraSpellsKnown;
	private final List<Spell> focusSpells;
	private final CasterType casterType;
	private SpellAbility(Builder builder) {
		super(builder);
		spellSlots = builder.spellSlots;
		extraSpellsKnown = builder.extraSpellsKnown;
		this.focusSpells = builder.focusSpells;
		casterType = builder.casterType;
	}

	public Map<Integer, Integer> getSpellSlots() {
		return Collections.unmodifiableMap(spellSlots);
	}

	public CasterType getCasterType() {
		return casterType;
	}

	public Map<Integer, Integer> getExtraSpellsKnown() {
		return Collections.unmodifiableMap(extraSpellsKnown);
	}

	public List<Spell> getFocusSpells() {
		return Collections.unmodifiableList(focusSpells);
	}

	public static class Builder extends Ability.Builder {
		private Map<Integer, Integer> spellSlots = Collections.emptyMap();
		private Map<Integer, Integer> extraSpellsKnown = Collections.emptyMap();
		private List<Spell> focusSpells = Collections.emptyList();
		private CasterType casterType = CasterType.None;
		public Builder() {}
		public Builder(Ability.Builder builder) {
			super(builder);
		}

		public void addSpellSlots(int level, int count) {
			if(spellSlots.size() == 0) spellSlots = new TreeMap<>();
			spellSlots.put(level, spellSlots.getOrDefault(level, 0) + count);
		}

		public void addExtraSpellKnown(int level, int count) {
			if(extraSpellsKnown.size() == 0) extraSpellsKnown = new TreeMap<>();
			extraSpellsKnown.put(level, extraSpellsKnown.getOrDefault(level, 0) + count);
		}

		public void addFocusSpell(Spell spell) {
			if(focusSpells.size() == 0) focusSpells = new ArrayList<>();
			focusSpells.add(spell);
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
