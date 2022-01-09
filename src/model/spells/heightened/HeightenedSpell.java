package model.spells.heightened;

import model.data_managers.sources.Source;
import model.enums.Trait;
import model.spells.*;

import java.util.List;

public class HeightenedSpell implements Spell {
    private final int heightenedLevel;
    private final Spell baseSpell;

    public HeightenedSpell(Spell spell, int heightenedLevel) {
        this.baseSpell = spell;
        this.heightenedLevel = heightenedLevel;
        if(!getHeightenedData().hasAtLevel(heightenedLevel))
            throw new SpellHeightenException(spell, heightenedLevel);
    }

    public Spell getBaseSpell() {
        return baseSpell;
    }

    @Override
    public String getName() {
        return baseSpell.getName();
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public String getRawName() {
        return baseSpell.getRawName();
    }

    @Override
    public String getDescription() {
        return baseSpell.getDescription();
    }

    @Override
    public String getRawDescription() {
        return baseSpell.getRawDescription();
    }

    @Override
    public int getPage() {
        return baseSpell.getPage();
    }

    @Override
    public Source getSourceBook() {
        return baseSpell.getSourceBook();
    }

    @Override
    public String getSource() {
        return baseSpell.getSource();
    }

    @Override
    public String getCastTime() {
        return baseSpell.getCastTime();
    }

    @Override
    public String getRequirements() {
        return baseSpell.getRequirements();
    }

    @Override
    public String getRange() {
        return baseSpell.getRange();
    }

    @Override
    public String getArea() {
        return baseSpell.getArea();
    }

    @Override
    public String getTargets() {
        return baseSpell.getTargets();
    }

    @Override
    public String getDuration() {
        return baseSpell.getDuration();
    }

    @Override
    public String getSave() {
        return baseSpell.getSave();
    }

    @Override
    public MagicalSchool getSchool() {
        return baseSpell.getSchool();
    }

    @Override
    public HeightenedData getHeightenedData() {
        return baseSpell.getHeightenedData();
    }

    @Override
    public int getLevel() {
        return heightenedLevel;
    }

    @Override
    public List<Trait> getTraits() {
        return baseSpell.getTraits();
    }

    @Override
    public List<Tradition> getTraditions() {
        return baseSpell.getTraditions();
    }

    @Override
    public List<SpellComponent> getComponents() {
        return baseSpell.getComponents();
    }

    @Override
    public int compareTo(Spell o) {
        return baseSpell.compareTo(o);
    }

    @Override
    public boolean isCantrip() {
        return baseSpell.isCantrip();
    }

    @Override
    public int getLevelOrCantrip() {
        return heightenedLevel;
    }
}
