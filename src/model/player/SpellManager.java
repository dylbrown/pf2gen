package model.player;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import model.abilities.SpellAbility;
import model.spells.CasterType;
import model.spells.Spell;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SpellManager {
	private ObservableMap<Integer, Integer> spellSlots = FXCollections.observableMap(new TreeMap<>());
	private ObservableMap<Integer, ObservableList<Spell>> spellsKnown = FXCollections.observableMap(new TreeMap<>());
	private List<CasterType> casterType = new ArrayList<>();

	SpellManager(Applier applier) {
		for(int i = 0; i <= 10; i++){
			spellSlots.put(i, 0);
			spellsKnown.put(i, FXCollections.observableArrayList());
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
		spellSlots.put(level, amount + spellSlots.getOrDefault(level, 0));
	}

	private void removeSlots(int level, int amount) {
		spellSlots.put(level, spellSlots.getOrDefault(level, 0) - amount);
	}

	private ObservableMap<Integer, Integer> slotsRetainer = FXCollections.unmodifiableObservableMap(spellSlots);
	public ObservableMap<Integer, Integer> getSpellSlots() {
		return slotsRetainer;
	}

	private Map<Integer, ObservableList<Spell>> knownRetainer = FXCollections.observableHashMap();
	public ObservableList<Spell> getSpellsKnown(int level) {
		return knownRetainer.computeIfAbsent(level, k->FXCollections.unmodifiableObservableList(
				spellsKnown.computeIfAbsent(level, (key)->FXCollections.observableArrayList())));
	}

	public boolean isCaster() {
		return casterType.size() > 0;
	}

	private CasterType getCasterType() {
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
		spellsKnown.computeIfAbsent(spell.getLevel(),(key)->FXCollections.observableArrayList())
				.remove(spell);
	}
}
