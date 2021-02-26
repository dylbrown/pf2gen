package model.data_managers.sources;

import model.spells.heightened.HeightenedSpell;
import model.spells.Spell;
import model.spells.Tradition;
import model.xml_parsers.FileLoader;
import model.xml_parsers.SpellsLoader;

import java.util.*;

public class SpellsMultiSourceLoader extends MultiSourceLoader<Spell> {
    private Map<Tradition, SortedMap<Integer, List<Spell>>> spellsByLevel = new HashMap<>();
    private Map<Tradition, SortedMap<Integer, Boolean>> heightenedLoaded = new HashMap<>();
    private Map<Tradition, SortedMap<Integer, List<Spell>>> heightenedSpellsByLevel = new HashMap<>();


    public SpellsMultiSourceLoader() {
        super("Spell");
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

    public List<Spell> getHeightenedSpells(Tradition tradition, int level) {
        if(level == 0 || level == 1)
            return Collections.emptyList();
        heightenedSpellsByLevel.computeIfAbsent(tradition, k -> new TreeMap<>());
        if (!heightenedLoaded.computeIfAbsent(tradition, s->new TreeMap<>()).containsKey(level - 1)) {
            for(int i = 1; i < level; i++) {
                if(heightenedLoaded.get(tradition).containsKey(i))
                    continue;
                for (Spell spell : getSpells(tradition, i)) {
                    for(int j = i + 1; j <= 10; j++) {
                        if(spell.getHeightenedData().hasAtLevel(j))
                            heightenedSpellsByLevel
                                    .get(tradition).computeIfAbsent(j, s->new ArrayList<>())
                                    .add(new HeightenedSpell(spell, j));
                    }
                }
                heightenedLoaded.get(tradition).put(i, true);
            }
        }

        return Collections.unmodifiableList(heightenedSpellsByLevel.get(tradition)
                .getOrDefault(level, Collections.emptyList()));
    }
}
