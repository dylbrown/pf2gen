package model.spells;

public enum MagicalSchool {
    Abjuration, Conjuration, Divination, Enchantment, Evocation, Illusion, Necromancy, Transmutation;

    public static MagicalSchool tryGet(String name) {
        for (MagicalSchool value : values()) {
            if(value.name().equalsIgnoreCase(name))
                return value;
        }
        return null;
    }
}
