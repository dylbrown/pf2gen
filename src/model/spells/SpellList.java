package model.spells;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.abilities.Ability;
import model.abilities.SpellExtension;
import model.ability_scores.AbilityScore;
import model.attributes.Attribute;
import model.attributes.BaseAttribute;
import model.player.PC;
import model.player.PlayerState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SpellList implements PlayerState {
	private final ObservableList<Integer> spellSlots = FXCollections.observableArrayList();
	private final ObservableList<ObservableList<Spell>> spellsKnown = FXCollections.observableArrayList();
	private final ObservableList<Integer> extraSpellsKnown = FXCollections.observableArrayList();
	private final ReadOnlyObjectWrapper<CasterType> casterType = new ReadOnlyObjectWrapper<>(CasterType.None);
	private final ReadOnlyObjectWrapper<Tradition> tradition = new ReadOnlyObjectWrapper<>();
	private final ReadOnlyObjectWrapper<AbilityScore> castingAbilityScore = new ReadOnlyObjectWrapper<>();
	private final ObservableList<Spell> focusSpells = FXCollections.observableArrayList();
	private final List<Spell> abilitySpells = new ArrayList<>();

	private final ObservableList<ObservableList<Spell>> knownRetainer = FXCollections.observableArrayList();
	private final String index;

	public SpellList(String index) {
		this.index = index;
		for(int i = 0; i <= 10; i++){
			spellSlots.add(0);
			extraSpellsKnown.add(0);
			spellsKnown.add(FXCollections.observableArrayList());
			knownRetainer.add(FXCollections.unmodifiableObservableList(spellsKnown.get(i)));
		}
	}

	public void apply(Ability ability) {
		SpellExtension spellExtension = ability.getExtension(SpellExtension.class);
		if(spellExtension != null){
			for (Map.Entry<Integer, Integer> entry : spellExtension.getSpellSlots().entrySet()) {
				addSlots(entry.getKey(), entry.getValue());
			}
			for (Map.Entry<Integer, Integer> entry : spellExtension.getExtraSpellsKnown().entrySet()) {
				addKnown(entry.getKey(), entry.getValue());
			}
			for (Map.Entry<SpellType, List<Spell>> entry : spellExtension.getBonusSpells().entrySet()) {
				switch(entry.getKey()) {
					case Spell:
					case Cantrip:
						entry.getValue().forEach(this::addBonusSpell);
						break;
					case Focus:
					case FocusCantrip:
						focusSpells.addAll(entry.getValue());
						break;
				}
			}
			if(spellExtension.getCasterType() != CasterType.None)
				casterType.set(spellExtension.getCasterType());
			if(spellExtension.getTradition() != null)
				tradition.set(spellExtension.getTradition());
			if(spellExtension.getCastingAbilityScore() != AbilityScore.None)
				castingAbilityScore.set(spellExtension.getCastingAbilityScore());

		}
	}

	public void remove(Ability ability) {
		SpellExtension spellExtension = ability.getExtension(SpellExtension.class);
		if(spellExtension != null){
			for (Map.Entry<Integer, Integer> entry : spellExtension.getSpellSlots().entrySet()) {
				removeSlots(entry.getKey(), entry.getValue());
			}
			for (Map.Entry<Integer, Integer> entry : spellExtension.getExtraSpellsKnown().entrySet()) {
				removeKnown(entry.getKey(), entry.getValue());
			}

			for (Map.Entry<SpellType, List<Spell>> entry : spellExtension.getBonusSpells().entrySet()) {
				switch(entry.getKey()) {
					case Spell:
					case Cantrip:
						entry.getValue().forEach(this::removeBonusSpell);
						break;
					case Focus:
					case FocusCantrip:
						focusSpells.removeAll(entry.getValue());
						break;
				}
			}
			if(spellExtension.getCasterType() != CasterType.None)
				casterType.set(CasterType.None);
			if(spellExtension.getTradition() != null)
				tradition.set(null);
			if(spellExtension.getCastingAbilityScore() != AbilityScore.None)
				castingAbilityScore.set(AbilityScore.None);
		}
	}

	public void addBonusSpell(Spell spell) {
		addSpellInternal(spell, spell.getLevelOrCantrip(),  true);
		addKnown(spell.getLevelOrCantrip(), 1);
		abilitySpells.add(spell);
	}

	public void removeBonusSpell(Spell spell) {
		removeSpellInternal(spell, true);
		removeKnown(spell.getLevelOrCantrip(), 1);
		abilitySpells.remove(spell);
	}

	public void addSlots(int level, int amount) {
		spellSlots.set(level, amount + spellSlots.get(level));
	}
	private void addKnown(int level, int amount) {
		extraSpellsKnown.set(level, amount + extraSpellsKnown.get(level));
	}

	public void removeSlots(int level, int amount) {
		spellSlots.set(level, spellSlots.get(level) - amount);
		checkKnownCap(level);
	}

	private void removeKnown(int level, int amount) {
		extraSpellsKnown.set(level, extraSpellsKnown.get(level) - amount);
		checkKnownCap(level);
	}

	private void checkKnownCap(int level) {
		if(getCasterType().get() == CasterType.Spontaneous) {
			while(spellsKnown.get(level).size() > spellSlots.get(level)) {
				spellsKnown.get(level).remove(spellsKnown.get(level).size() - 1);
			}
		}
	}

	private final ObservableList<Integer> slotsRetainer = FXCollections.unmodifiableObservableList(spellSlots);
	public ObservableList<Integer> getSpellSlots() {
		return slotsRetainer;
	}

	private final ObservableList<Spell> focusRetainer = FXCollections.unmodifiableObservableList(focusSpells);
	public ObservableList<Spell> getFocusSpells() {
		return focusRetainer;
	}

	private final ObservableList<ObservableList<Spell>> nestedKnown = FXCollections.unmodifiableObservableList(knownRetainer);
	public ObservableList<ObservableList<Spell>> getSpellsKnown() {
		return nestedKnown;
	}

	public ObservableList<Spell> getSpellsKnown(int level) {
		return knownRetainer.get(level);
	}

	private final ObservableList<Integer> extraRetainer = FXCollections.unmodifiableObservableList(extraSpellsKnown);
	public ObservableList<Integer> getExtraSpellsKnown() {
		return extraRetainer;
	}

	public boolean isCaster() {
		return casterType.get() != null && casterType.get() != CasterType.None;
	}

	public ReadOnlyObjectProperty<CasterType> getCasterType() {
		return casterType.getReadOnlyProperty();
	}

	public boolean addSpell(Spell spell) {
		return addSpellInternal(spell, spell.getLevelOrCantrip(), false);
	}

	protected boolean addSpellInternal(Spell spell, int level, boolean override) {
		if(spell == null) return false;
		if(getCasterType().get() == CasterType.None && !override) return false;
		if(getCasterType().get() == CasterType.Spontaneous) {
			if(spellsKnown.get(level).size()
					>= spellSlots.get(level) + extraSpellsKnown.get(level)
					&& !override)
				return false;
		}
		ObservableList<Spell> levelList = spellsKnown.get(level);
		if(levelList.contains(spell)) return false;
		levelList.add(spell);
		return true;

	}

	public boolean removeSpell(Spell spell) {
		return removeSpellInternal(spell, false);
	}

	private boolean removeSpellInternal(Spell spell, boolean override) {
		if(abilitySpells.contains(spell) && !override) return false;
		spellsKnown.get(spell.getLevelOrCantrip()).remove(spell);
		return true;
	}

	public ReadOnlyObjectProperty<Tradition> getTradition() {
		return tradition.getReadOnlyProperty();
	}

	public Attribute getSpellDCsAttribute() {
		switch (tradition.get()) {
			case Arcane: return BaseAttribute.ArcaneSpellDCs;
			case Divine: return BaseAttribute.DivineSpellDCs;
			case Occult: return BaseAttribute.OccultSpellDCs;
			case Primal: return BaseAttribute.PrimalSpellDCs;
		}
		return BaseAttribute.None;
	}

	public Attribute getSpellAttacksAttribute() {
		switch (tradition.get()) {
			case Arcane: return BaseAttribute.ArcaneSpellAttacks;
			case Divine: return BaseAttribute.DivineSpellAttacks;
			case Occult: return BaseAttribute.OccultSpellAttacks;
			case Primal: return BaseAttribute.PrimalSpellAttacks;
		}
		return BaseAttribute.None;
	}

	public int getFocusPointCount() {
		return Math.min(focusSpells.size(), 3);
	}

	public void addFocusSpell(Spell spell) {
		focusSpells.add(spell);
	}

	public void removeFocusSpell(Spell spell) {
		focusSpells.remove(spell);
	}

	public ReadOnlyObjectProperty<AbilityScore> getCastingAbility() {
		return castingAbilityScore.getReadOnlyProperty();
	}

	@Override
	public void reset(PC.ResetEvent resetEvent) {
		for(int i=0; i <= 10; i++) {
			spellsKnown.get(i).clear();
		}
	}

	public String getIndex() {
		return index;
	}

	public int getHighestLevelCanCast() {
		for(int i = 0; i <spellSlots.size(); i++) {
			if(spellSlots.get(i) <= 0)
				return i - 1;
		}
		return spellSlots.size();
	}
}
