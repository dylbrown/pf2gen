package model.data_managers;

import model.spells.Spell;
import model.xml_parsers.SpellsLoader;

import java.util.*;

public class AllSpells {
	private static final SortedMap<String, Spell> allSpellsMap;
	private static final SortedMap<Integer, List<Spell>> spellsByLevel;
	private static final SpellsLoader spells = new SpellsLoader();

	static{
		allSpellsMap = new TreeMap<>();
		spellsByLevel = new TreeMap<>();
		for (Spell spell : getAllSpells()) {
			allSpellsMap.put(spell.toString().toLowerCase(), spell);
			spellsByLevel.computeIfAbsent(spell.getLevel(), (key)->new ArrayList<>()).add(spell);
		}
	}

	private static List<Spell> getAllSpells() {
		return spells.parse();
	}

	public static List<Spell> getSpells(int level) {return spellsByLevel.get(level);}

	public static Spell find(String contents) {
		return allSpellsMap.get(contents.toLowerCase());
	}
}
