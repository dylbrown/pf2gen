package model.spells.heightened;

import model.spells.Spell;

public class NotHeightenable implements HeightenedData {

    private final Spell spell;

    public NotHeightenable(Spell spell) {
        this.spell = spell;
    }

    @Override
    public boolean hasAtLevel(int level) {
        return level == spell.getLevel();
    }

    @Override
    public String descriptionAtLevel(int level) {
        return null;
    }

    @Override
    public Spell getSpell() {
        return spell;
    }
}
