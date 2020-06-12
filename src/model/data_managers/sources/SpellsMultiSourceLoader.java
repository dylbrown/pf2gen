package model.data_managers.sources;

import model.spells.Spell;
import model.spells.Tradition;
import model.xml_parsers.FileLoader;
import model.xml_parsers.SpellsLoader;

import java.util.*;

public class SpellsMultiSourceLoader extends MultiSourceLoader<Spell> {
    private Map<Tradition, SortedMap<Integer, List<Spell>>> spellsByLevel = new HashMap<>();
    public SpellsMultiSourceLoader(List<? extends FileLoader<Spell>> fileLoaders) {
        super(fileLoaders);
    }

    public List<Spell> getSpells(Tradition tradition, int level) {
        spellsByLevel.computeIfAbsent(tradition, k -> new TreeMap<>());
        if(spellsByLevel.get(tradition).get(level) == null) {
            List<Spell> spells = new ArrayList<>();
            for (FileLoader<Spell> loader : loaders) {
                if(loader instanceof SpellsLoader) {
                    spells.addAll(((SpellsLoader) loader).getSpells(tradition, level));
                }
            }
            spellsByLevel.get(tradition).put(level, spells);
        }

        return Collections.unmodifiableList(spellsByLevel.get(tradition).get(level));
    }
}
