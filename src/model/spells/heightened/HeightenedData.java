package model.spells.heightened;

import model.spells.Spell;

public interface HeightenedData {
    /**
     * Does the spell have a heightened effect at that level
      * @param level The level to check
     * @return returns true if there is a heightened effect at that level
     */
    boolean hasAtLevel(int level);
    /**
     * Get the heightened effect description at that level
     * @param level The level to check
     * @return returns the heightened effect description if it exists, otherwise null
     */
    String descriptionAtLevel(int level);
    Spell getSpell();
}
