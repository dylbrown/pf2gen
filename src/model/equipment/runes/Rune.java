package model.equipment.runes;

import model.equipment.Equipment;
import model.util.StringUtils;

public class Rune extends Equipment {
    private final boolean fundamental;
    private final int grantsProperty;

    Rune(Rune.Builder builder) {
        super(builder);
        fundamental = builder.fundamental;
        grantsProperty = builder.grantsProperty;
    }

    public String getBaseRune() {
        int end = getName().indexOf("(");
        if(end == -1)
            end = getName().length();
        return getName().substring(0, end).trim();
    }

    public boolean isFundamental() {
        return fundamental;
    }

    public int getGrantsProperty() {
        return grantsProperty;
    }

    public String getTier() {
        return StringUtils.getInBrackets(getName());
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
