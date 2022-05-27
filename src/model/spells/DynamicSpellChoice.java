package model.spells;

import model.player.ArbitraryChoice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DynamicSpellChoice extends ArbitraryChoice<Spell> {
    private final List<Integer> levels;
    private final List<Tradition> traditions;

    private DynamicSpellChoice(Builder builder) {
        super(builder);
        levels = builder.levels;
        traditions = builder.traditions;
    }

    @Override
    public DynamicSpellChoice copy() {
        return new Builder(this).build();
    }

    public List<Integer> getLevels() {
        return Collections.unmodifiableList(levels);
    }

    public List<Tradition> getTraditions() {
        return Collections.unmodifiableList(traditions);
    }

    public static class Builder extends ArbitraryChoice.Builder<Spell> {
        private List<Integer> levels = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        private List<Tradition> traditions = Arrays.asList(Tradition.values());

        public Builder() {
            setOptionsClass(Spell.class);
        }

        public Builder(DynamicSpellChoice other) {
            super(other);
            levels = other.levels;
            traditions = other.traditions;
        }

        public void addTradition(Tradition tradition) {
            if(traditions.size() == Tradition.values().length)
                traditions = new ArrayList<>();
            traditions.add(tradition);
        }

        public void addLevel(int level) {
            if(levels.size() == 11)
                levels = new ArrayList<>();
            levels.add(level);
        }

        public DynamicSpellChoice build() {
            return new DynamicSpellChoice(this);
        }
    }
}
