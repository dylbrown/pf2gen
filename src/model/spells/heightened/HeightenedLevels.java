package model.spells.heightened;

import model.spells.Spell;

import java.util.HashMap;
import java.util.Map;

public class HeightenedLevels implements HeightenedData {
    private final Map<Integer, String> levels;
    private final Spell spell;

    private HeightenedLevels(Map<Integer, String> levels, Spell spell) {
        this.levels = levels;
        this.spell = spell;
    }


    @Override
    public boolean hasAtLevel(int level) {
        return levels.containsKey(level);
    }

    @Override
    public String descriptionAtLevel(int level) {
        return levels.get(level);
    }

    @Override
    public Spell getSpell() {
        return spell;
    }

    public static class Builder {
        private final Map<Integer, String> levels = new HashMap<>();
        private Spell spell;

        public void add(int level, String description) {
            this.levels.put(level, description);
        }

        public void setSpell(Spell spell) {
            this.spell = spell;
        }

        public HeightenedLevels build() {
            return new HeightenedLevels(levels, spell);
        }
    }
}
