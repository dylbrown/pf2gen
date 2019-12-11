package model.player;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.abilities.SpellAbility;
import model.spells.CasterType;
import model.spells.Spell;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SpellManager {
	private ObservableList<Integer> spellSlots = FXCollections.observableArrayList();
	private ObservableList<ObservableList<Spell>> spellsKnown = FXCollections.observableArrayList();
	private List<CasterType> casterType = new ArrayList<>();

	private ObservableList<ObservableList<Spell>> knownRetainer = FXCollections.observableArrayList();

	SpellManager(Applier applier) {
		for(int i = 0; i <= 10; i++){
			spellSlots.add(0);
			spellsKnown.add(FXCollections.observableArrayList());
			knownRetainer.add(FXCollections.unmodifiableObservableList(spellsKnown.get(i)));
		}

		applier.onApply(ability -> {
			if(ability instanceof SpellAbility){
				SpellAbility sAbility = (SpellAbility) ability;
				for (Map.Entry<Integer, Integer> entry : sAbility.getSpellSlots().entrySet()) {
					addSlots(entry.getKey(), entry.getValue());
				}
				if(sAbility.getCasterType() != CasterType.None)
					casterType.add(sAbility.getCasterType());
			}
		});

		applier.onRemove(ability -> {
			if(ability instanceof SpellAbility){
				SpellAbility sAbility = (SpellAbility) ability;
				for (Map.Entry<Integer, Integer> entry : sAbility.getSpellSlots().entrySet()) {
					removeSlots(entry.getKey(), entry.getValue());
				}
				if(sAbility.getCasterType() != CasterType.None)
					casterType.remove(sAbility.getCasterType());
			}
		});
	}

	private void addSlots(int level, int amount) {
		spellSlots.set(level, amount + spellSlots.get(level));
	}

	private void removeSlots(int level, int amount) {
		spellSlots.set(level, spellSlots.get(level) - amount);
		if(getCasterType() == CasterType.Spontaneous) {
			while(spellsKnown.get(level).size() > spellSlots.get(level)) {
				spellsKnown.remove(spellsKnown.size() - 1);
			}
		}
	}

	private ObservableList<Integer> slotsRetainer = FXCollections.unmodifiableObservableList(spellSlots);
	public ObservableList<Integer> getSpellSlots() {
		return slotsRetainer;
	}

	private ObservableList<ObservableList<Spell>> nestedKnown = FXCollections.unmodifiableObservableList(knownRetainer);
	public ObservableList<ObservableList<Spell>> getSpellsKnown() {
		return nestedKnown;
	}

	public ObservableList<Spell> getSpellsKnown(int level) {
		return knownRetainer.get(level);
	}

	public boolean isCaster() {
		return casterType.size() > 0;
	}

	public CasterType getCasterType() {
		return (isCaster()) ? casterType.get(0) : CasterType.None;
	}

	public boolean addSpell(Spell spell) {
		if(getCasterType() == CasterType.None) return false;
		if(getCasterType() == CasterType.Spontaneous) {
			if(spellsKnown.get(spell.getLevel()).size()
					>= spellSlots.get(spell.getLevel()))
				return false;
		}
		ObservableList<Spell> levelList = spellsKnown.get(spell.getLevel());
		if(levelList.contains(spell)) return false;
		levelList.add(spell);
		return true;
	}

	public void removeSpell(Spell spell) {
		spellsKnown.get(spell.getLevel()).remove(spell);
	}

	public void reset() {
		for(int i=0; i <= 10; i++) {
			spellsKnown.get(i).clear();
		}
	}
}
