package model.abilities;

import model.spells.CasterType;
import model.spells.Spell;
import model.spells.SpellType;
import model.spells.Tradition;

import java.util.*;

public class SpellAbility extends Ability {
	private final Map<Integer, Integer> spellSlots;
	private final Map<Integer, Integer> extraSpellsKnown;
	private final Tradition tradition;
	private Map<SpellType, List<Spell>> bonusSpells = Collections.emptyMap();
	private final CasterType casterType;
	private SpellAbility(Builder builder) {
		super(builder);
		spellSlots = builder.spellSlots;
		extraSpellsKnown = builder.extraSpellsKnown;
		casterType = builder.casterType;
		tradition = builder.tradition;

		if(builder.bonusSpells.size() > 0) bonusSpells = new HashMap<>();
		for (Map.Entry<SpellType, List<Spell>> entry : builder.bonusSpells.entrySet()) {
			bonusSpells.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
		}

	}

	public Map<Integer, Integer> getSpellSlots() {
		return Collections.unmodifiableMap(spellSlots);
	}

	public CasterType getCasterType() {
		return casterType;
	}

	public Tradition getTradition() {
		return tradition;
	}

	public Map<Integer, Integer> getExtraSpellsKnown() {
		return Collections.unmodifiableMap(extraSpellsKnown);
	}

	public Map<SpellType, List<Spell>> getBonusSpells() {
		return Collections.unmodifiableMap(bonusSpells);
	}

	public static class Builder extends Ability.Builder {
		private Map<Integer, Integer> spellSlots = Collections.emptyMap();
		private Map<Integer, Integer> extraSpellsKnown = Collections.emptyMap();
		private Map<SpellType, List<Spell>> bonusSpells = Collections.emptyMap();
		private CasterType casterType = CasterType.None;
		private Tradition tradition;

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

		public void addBonusSpell(SpellType type, Spell spell) {
			if(bonusSpells.size() == 0) bonusSpells = new HashMap<>();
			bonusSpells.computeIfAbsent(type, k -> new ArrayList<>()).add(spell);
		}

		public void setCasterType(CasterType casterType) {
			this.casterType = casterType;
		}

		@Override
		public SpellAbility build() {
			return new SpellAbility(this);
		}

		public void setTradition(Tradition tradition) {
			this.tradition = tradition;
		}
	}
}
