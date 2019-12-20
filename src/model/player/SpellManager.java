package model.player;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.abilities.SpellAbility;
import model.spells.CasterType;
import model.spells.Spell;
import model.spells.SpellType;
import model.spells.Tradition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SpellManager {
	private ObservableList<Integer> spellSlots = FXCollections.observableArrayList();
	private ObservableList<ObservableList<Spell>> spellsKnown = FXCollections.observableArrayList();
	private ObservableList<Integer> extraSpellsKnown = FXCollections.observableArrayList();
	private ReadOnlyObjectWrapper<CasterType> casterType = new ReadOnlyObjectWrapper<>();
	private ReadOnlyObjectWrapper<Tradition> tradition = new ReadOnlyObjectWrapper<>();
	private ObservableList<Spell> focusSpells = FXCollections.observableArrayList();
	private List<Spell> abilitySpells = new ArrayList<>();

	private ObservableList<ObservableList<Spell>> knownRetainer = FXCollections.observableArrayList();

	SpellManager(Applier applier) {
		for(int i = 0; i <= 10; i++){
			spellSlots.add(0);
			extraSpellsKnown.add(0);
			spellsKnown.add(FXCollections.observableArrayList());
			knownRetainer.add(FXCollections.unmodifiableObservableList(spellsKnown.get(i)));
		}

		applier.onApply(ability -> {
			if(ability instanceof SpellAbility){
				SpellAbility sAbility = (SpellAbility) ability;
				for (Map.Entry<Integer, Integer> entry : sAbility.getSpellSlots().entrySet()) {
					addSlots(entry.getKey(), entry.getValue());
				}
				for (Map.Entry<Integer, Integer> entry : sAbility.getExtraSpellsKnown().entrySet()) {
					addKnown(entry.getKey(), entry.getValue());
				}
				for (Map.Entry<SpellType, List<Spell>> entry : sAbility.getBonusSpells().entrySet()) {
					switch(entry.getKey()) {
						case Spell:
						case Cantrip:
							entry.getValue().forEach(this::addSpell);
							abilitySpells.addAll(entry.getValue());
							break;
						case Focus:
						case FocusCantrip:
							focusSpells.addAll(entry.getValue());
							break;
					}
				}
				if(sAbility.getCasterType() != CasterType.None)
					casterType.set(sAbility.getCasterType());
				if(sAbility.getTradition() != null)
					tradition.set(sAbility.getTradition());
			}
		});

		applier.onRemove(ability -> {
			if(ability instanceof SpellAbility){
				SpellAbility sAbility = (SpellAbility) ability;
				for (Map.Entry<Integer, Integer> entry : sAbility.getSpellSlots().entrySet()) {
					removeSlots(entry.getKey(), entry.getValue());
				}
				for (Map.Entry<Integer, Integer> entry : sAbility.getExtraSpellsKnown().entrySet()) {
					removeKnown(entry.getKey(), entry.getValue());
				}
				for (Map.Entry<SpellType, List<Spell>> entry : sAbility.getBonusSpells().entrySet()) {
					switch(entry.getKey()) {
						case Spell:
						case Cantrip:
							abilitySpells.removeAll(entry.getValue());
							entry.getValue().forEach(this::removeSpell);
							break;
						case Focus:
						case FocusCantrip:
							focusSpells.removeAll(entry.getValue());
							break;
					}
				}
				if(sAbility.getCasterType() != CasterType.None)
					casterType.set(CasterType.None);
				if(sAbility.getTradition() != null)
					tradition.set(null);
			}
		});
	}

	private void addSlots(int level, int amount) {
		spellSlots.set(level, amount + spellSlots.get(level));
	}
	private void addKnown(int level, int amount) {
		extraSpellsKnown.set(level, amount + extraSpellsKnown.get(level));
	}

	private void removeSlots(int level, int amount) {
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

	private ObservableList<Integer> slotsRetainer = FXCollections.unmodifiableObservableList(spellSlots);
	public ObservableList<Integer> getSpellSlots() {
		return slotsRetainer;
	}

	private ObservableList<Spell> focusRetainer = FXCollections.unmodifiableObservableList(focusSpells);
	public ObservableList<Spell> getFocusSpells() {
		return focusRetainer;
	}

	private ObservableList<ObservableList<Spell>> nestedKnown = FXCollections.unmodifiableObservableList(knownRetainer);
	public ObservableList<ObservableList<Spell>> getSpellsKnown() {
		return nestedKnown;
	}

	public ObservableList<Spell> getSpellsKnown(int level) {
		return knownRetainer.get(level);
	}

	private ObservableList<Integer> extraRetainer = FXCollections.unmodifiableObservableList(extraSpellsKnown);
	public ObservableList<Integer> getExtraSpellsKnown() {
		return extraSpellsKnown;
	}

	public boolean isCaster() {
		return casterType.get() != null && casterType.get() != CasterType.None;
	}

	public ReadOnlyObjectProperty<CasterType> getCasterType() {
		return casterType.getReadOnlyProperty();
	}

	public boolean addSpell(Spell spell) {
		if(getCasterType().get() == CasterType.None) return false;
		if(getCasterType().get() == CasterType.Spontaneous) {
			if(spellsKnown.get(spell.getLevelOrCantrip()).size()
					>= spellSlots.get(spell.getLevelOrCantrip()) + extraSpellsKnown.get(spell.getLevelOrCantrip()))
				return false;
		}
		ObservableList<Spell> levelList = spellsKnown.get(spell.getLevelOrCantrip());
		if(levelList.contains(spell)) return false;
		levelList.add(spell);
		return true;
	}

	public boolean removeSpell(Spell spell) {
		if(abilitySpells.contains(spell)) return false;
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
}
