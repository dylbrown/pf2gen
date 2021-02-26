package model.spells;

public class SpellHeightenException extends RuntimeException {
    public SpellHeightenException(Spell spell, int heightenedLevel) {
        super("Attempted to heighten" + spell.getName() + " to level" + heightenedLevel);
    }
}
