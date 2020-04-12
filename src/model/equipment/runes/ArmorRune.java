package model.equipment.runes;

public class ArmorRune extends Rune {
    private final int bonusAC;
    private ArmorRune(Builder builder) {
        super(builder);
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

        public ArmorRune build() {
            return new ArmorRune(this);
        }
    }
}
