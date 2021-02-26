package model.items.runes;

import model.items.Item;

public class ArmorRune extends Rune {
    private final int bonusAC;
    private ArmorRune(Builder builder, Item baseItem) {
        super(builder, baseItem);
        bonusAC = builder.bonusAC;

    }

    public int getBonusAC() {
        return bonusAC;
    }

    public static class Builder extends Rune.Builder {
        private int bonusAC;

        public void setBonusAC(int bonusAC) {
            this.bonusAC = bonusAC;
        }

        public ArmorRune build(Item baseItem) {
            return new ArmorRune(this, baseItem);
        }
    }
}
