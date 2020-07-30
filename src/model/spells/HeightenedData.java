package model.spells;

public interface HeightenedData {
    boolean hasAtLevel(int level);
    String descriptionAtLevel(int level);
    Spell getSpell();
}
