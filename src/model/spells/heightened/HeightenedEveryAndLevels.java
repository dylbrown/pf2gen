package model.spells.heightened;

import model.spells.Spell;

public class HeightenedEveryAndLevels implements HeightenedData {

    private final HeightenedEvery every;
    private final HeightenedLevels levels;
    private final Spell spell;

    private HeightenedEveryAndLevels(Builder builder) {
        every = builder.every.build();
        levels = builder.levels.build();
        spell = builder.spell;
    }

    @Override
    public boolean hasAtLevel(int level) {
        return every.hasAtLevel(level) || levels.hasAtLevel(level);
    }

    @Override
    public String descriptionAtLevel(int level) {
        String everyDesc = every.descriptionAtLevel(level);
        String levelsDesc = levels.descriptionAtLevel(level);
        if(everyDesc == null && levelsDesc == null)
            return null;
        if(everyDesc == null)
            return levelsDesc;
        if(levelsDesc == null)
            return everyDesc;
        return everyDesc + "<br>" + levelsDesc;
    }

    @Override
    public Spell getSpell() {
        return spell;
    }

    public static class Builder {
        private HeightenedEvery.Builder every;
        private HeightenedLevels.Builder levels;
        private Spell spell;

        public void setEvery(HeightenedEvery.Builder every) {
            this.every = every;
        }

        public void setLevels(HeightenedLevels.Builder levels) {
            this.levels = levels;
        }

        public void setSpell(Spell spell) {
            this.spell = spell;
        }

        public HeightenedEveryAndLevels build() {
            every.setSpell(spell);
            levels.setSpell(spell);
            return new HeightenedEveryAndLevels(this);
        }
    }
}
