package model.data_managers;

import model.spells.Spell;
import model.xml_parsers.SpellsLoader;

import java.util.*;

public class AllSpells {
	private static final SortedMap<String, Spell> allSpellsMap;
	private static final SortedMap<String, Spell> allFocusSpellsMap;
	private static final SortedMap<Integer, List<Spell>> spellsByLevel;
	private static final SpellsLoader spells = new SpellsLoader("spells.pfdyl");
	private static final SpellsLoader focusSpells = new SpellsLoader("focusSpells.pfdyl");

	static{
		allSpellsMap = new TreeMap<>();
		allFocusSpellsMap = new TreeMap<>();
		spellsByLevel = new TreeMap<>();
		for (Spell spell : getAllSpells()) {
			allSpellsMap.put(spell.toString().toLowerCase(), spell);
			spellsByLevel.computeIfAbsent(spell.isCantrip() ? 0 : spell.getLevel(), (key)->new ArrayList<>()).add(spell);
		}
		for (Spell spell : getAllFocusSpells()) {
			allFocusSpellsMap.put(spell.toString().toLowerCase(), spell);
		}

	}

	public static List<Spell> getAllSpells() {
		return spells.parse();
	}

	public static List<Spell> getAllFocusSpells() {
		return focusSpells.parse();
	}

	public static List<Spell> getSpells(int level) {return spellsByLevel.get(level);}

	public static Spell find(String contents) {
		Spell spell = allSpellsMap.get(contents.toLowerCase());
		return (spell == null) ? allFocusSpellsMap.get(contents.toLowerCase()): spell;
	}
}
