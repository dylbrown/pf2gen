package model.spells.heightened;

import model.spells.Spell;

public interface HeightenedData {
    // Does the spell have a heightened effect at that level
    boolean hasAtLevel(int level);
    // The description of the heightened effect at that level
    String descriptionAtLevel(int level);
    Spell getSpell();
}
