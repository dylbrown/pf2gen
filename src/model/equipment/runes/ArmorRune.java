package model.equipment.runes;

import model.attributes.AttributeBonus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArmorRune extends Rune {
    private final int bonusAC;
    ArmorRune(Builder builder) {
        super(builder);
        bonusAC = builder.bonusAC;

    }

    public int getBonusAC() {
        return bonusAC;
    }

    public static class Builder extends Rune.Builder {
        private int bonusAC;
        private List<AttributeBonus> bonuses = Collections.emptyList();

        public void setBonusAC(int bonusAC) {
            this.bonusAC = bonusAC;
        }

        public void addBonus(AttributeBonus bonus) {
            if(bonuses.size() == 0) bonuses = new ArrayList<>();
            bonuses.add(bonus);
        }

        public ArmorRune build() {
            return new ArmorRune(this);
        }
    }
}
