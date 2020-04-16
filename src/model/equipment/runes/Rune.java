package model.equipment.runes;

import model.equipment.Equipment;

public class Rune extends Equipment {
    private final boolean fundamental;
    private final int grantsProperty;

    Rune(Rune.Builder builder) {
        super(builder);
        fundamental = builder.fundamental;
        grantsProperty = builder.grantsProperty;
    }

    public String getBaseRune() {return getName().replaceAll(" ?\\([^)]*\\)", "").trim();}

    public boolean isFundamental() {
        return fundamental;
    }

    public int getGrantsProperty() {
        return grantsProperty;
    }

    public String getTier() {
        return getName().replaceAll("(.*\\(|\\).*)", "").trim();
    }

    public static class Builder extends Equipment.Builder {
        private boolean fundamental = false;
        private int grantsProperty = 0;

        public void setFundamental(boolean fundamental) {
            this.fundamental = fundamental;
        }

        public void setGrantsProperties(int grantsProperty) {
            this.grantsProperty = grantsProperty;
        }

        public Rune build() {
            return new Rune(this);
        }
    }
}
