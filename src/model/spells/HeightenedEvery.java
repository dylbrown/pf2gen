package model.spells;

public class HeightenedEvery implements HeightenedData {
    private final int every;
    private final String description;
    private final Spell spell;

    private HeightenedEvery(int every, String description, Spell spell) {
        this.every = every;
        this.description = description;
        this.spell = spell;
    }

    public int getEvery() {
        return every;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean hasAtLevel(int level) {
        return level > spell.getLevel() && (level - spell.getLevel()) % every == 0;
    }

    @Override
    public String descriptionAtLevel(int level) {
        return (hasAtLevel(level)) ? description : null;
    }

    @Override
    public Spell getSpell() {
        return spell;
    }

    public static class Builder {
        private int every;
        private String description;
        private Spell spell;

        public void setEvery(int every) {
            this.every = every;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setSpell(Spell spell) {
            this.spell = spell;
        }

        public HeightenedEvery build() {
            return new HeightenedEvery(every, description, spell);
        }
    }
}
