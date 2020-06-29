package model.player;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.abilities.SpellExtension;
import model.ability_scores.AbilityScore;
import model.attributes.Attribute;
import model.spells.CasterType;
import model.spells.Spell;
import model.spells.SpellType;
import model.spells.Tradition;

import java.util.*;

public class SpellManager {
	private final ObservableList<Integer> spellSlots = FXCollections.observableArrayList();
	private final ObservableList<ObservableList<Spell>> spellsKnown = FXCollections.observableArrayList();
	private final ObservableList<Integer> extraSpellsKnown = FXCollections.observableArrayList();
	private final ReadOnlyObjectWrapper<CasterType> casterType = new ReadOnlyObjectWrapper<>(CasterType.None);
	private final ReadOnlyObjectWrapper<Tradition> tradition = new ReadOnlyObjectWrapper<>();
	private final ReadOnlyObjectWrapper<AbilityScore> castingAbilityScore = new ReadOnlyObjectWrapper<>();
	private final ObservableList<Spell> focusSpells = FXCollections.observableArrayList();
	private final List<Spell> abilitySpells = new ArrayList<>();

	private final ObservableList<ObservableList<Spell>> knownRetainer = FXCollections.observableArrayList();

	SpellManager(Applier applier) {
		for(int i = 0; i <= 10; i++){
			spellSlots.add(0);
			extraSpellsKnown.add(0);
			spellsKnown.add(FXCollections.observableArrayList());
			knownRetainer.add(FXCollections.unmodifiableObservableList(spellsKnown.get(i)));
		}

		applier.onApply(ability -> {
			SpellExtension spellExtension = ability.getExtension(SpellExtension.class);
			if(spellExtension != null){
				for (Map.Entry<Integer, Integer> entry : spellExtension.getSpellSlots().entrySet()) {
					addSlots(entry.getKey(), entry.getValue());
				}
				for (Map.Entry<Integer, Integer> entry : spellExtension.getExtraSpellsKnown().entrySet()) {
					addKnown(entry.getKey(), entry.getValue());
				}
				addBonusSpells(spellExtension);
				if(spellExtension.getCasterType() != CasterType.None)
					casterType.set(spellExtension.getCasterType());
				if(spellExtension.getTradition() != null)
					tradition.set(spellExtension.getTradition());
				if(spellExtension.getCastingAbilityScore() != AbilityScore.None)
					castingAbilityScore.set(spellExtension.getCastingAbilityScore());

			}
		});

		applier.onRemove(ability -> {
			SpellExtension spellExtension = ability.getExtension(SpellExtension.class);
			if(spellExtension != null){
				for (Map.Entry<Integer, Integer> entry : spellExtension.getSpellSlots().entrySet()) {
					removeSlots(entry.getKey(), entry.getValue());
				}
				for (Map.Entry<Integer, Integer> entry : spellExtension.getExtraSpellsKnown().entrySet()) {
					removeKnown(entry.getKey(), entry.getValue());
				}
				removeBonusSpells(spellExtension);
				if(spellExtension.getCasterType() != CasterType.None)
					casterType.set(CasterType.None);
				if(spellExtension.getTradition() != null)
					tradition.set(null);
				if(spellExtension.getCastingAbilityScore() != AbilityScore.None)
					castingAbilityScore.set(AbilityScore.None);
			}
		});
	}

	private void addBonusSpells(SpellExtension spellExtension) {
		for (Map.Entry<SpellType, List<Spell>> entry : spellExtension.getBonusSpells().entrySet()) {
			switch(entry.getKey()) {
				case Spell:
				case Cantrip:
					entry.getValue().forEach(s->{
						if(addSpellInternal(s, true))
							addKnown(s.getLevelOrCantrip(), 1);
					});
					abilitySpells.addAll(entry.getValue());
					break;
				case Focus:
				case FocusCantrip:
					focusSpells.addAll(entry.getValue());
					break;
			}
		}
	}

	private void removeBonusSpells(SpellExtension spellExtension) {
		for (Map.Entry<SpellType, List<Spell>> entry : spellExtension.getBonusSpells().entrySet()) {
			switch(entry.getKey()) {
				case Spell:
				case Cantrip:
					abilitySpells.removeAll(entry.getValue());
					entry.getValue().forEach(s->{
						if(removeSpellInternal(s, true))
							removeKnown(s.getLevelOrCantrip(), 1);
					});
					break;
				case Focus:
				case FocusCantrip:
					focusSpells.removeAll(entry.getValue());
					break;
			}
		}
	}

	void addSlots(int level, int amount) {
		spellSlots.set(level, amount + spellSlots.get(level));
	}
	private void addKnown(int level, int amount) {
		extraSpellsKnown.set(level, amount + extraSpellsKnown.get(level));
	}

	void removeSlots(int level, int amount) {
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
		return addSpellInternal(spell, false);
	}

	private boolean addSpellInternal(Spell spell, boolean override) {
		if(spell == null) return false;
		if(getCasterType().get() == CasterType.None && !override) return false;
		if(getCasterType().get() == CasterType.Spontaneous) {
			if(spellsKnown.get(spell.getLevelOrCantrip()).size()
					>= spellSlots.get(spell.getLevelOrCantrip()) + extraSpellsKnown.get(spell.getLevelOrCantrip())
					&& !override)
				return false;
		}
		ObservableList<Spell> levelList = spellsKnown.get(spell.getLevelOrCantrip());
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

	public void reset() {
		for(int i=0; i <= 10; i++) {
			spellsKnown.get(i).clear();
		}
	}

	public ReadOnlyObjectProperty<Tradition> getTradition() {
		return tradition.getReadOnlyProperty();
	}

	public Attribute getSpellDCsAttribute() {
		switch (tradition.get()) {
			case Arcane: return Attribute.ArcaneSpellDCs;
			case Divine: return Attribute.DivineSpellDCs;
			case Occult: return Attribute.OccultSpellDCs;
			case Primal: return Attribute.PrimalSpellDCs;
		}
		return Attribute.None;
	}

	public Attribute getSpellAttacksAttribute() {
		switch (tradition.get()) {
			case Arcane: return Attribute.ArcaneSpellAttacks;
			case Divine: return Attribute.DivineSpellAttacks;
			case Occult: return Attribute.OccultSpellAttacks;
			case Primal: return Attribute.PrimalSpellAttacks;
		}
		return Attribute.None;
	}

	public int getFocusPointCount() {
		return Math.min(focusSpells.size(), 3);
	}

	void addFocusSpell(Spell spell) {
		focusSpells.add(spell);
	}

	void removeFocusSpell(Spell spell) {
		focusSpells.remove(spell);
	}

	public ReadOnlyObjectProperty<AbilityScore> getCastingAbility() {
		return castingAbilityScore.getReadOnlyProperty();
	}
}
