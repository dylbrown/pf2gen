package model.items.runes;

import model.items.Item;
import model.items.ItemExtension;
import model.util.StringUtils;

public abstract class Rune extends ItemExtension {

    public static Rune getRune(Item item) {
        Rune rune;
        rune = item.getExtension(ArmorRune.class);
        if(rune != null) return rune;
        rune = item.getExtension(WeaponRune.class);
        return rune;
    }

    public static boolean isRune(Item item) {
        if(item.hasExtension(ArmorRune.class)) return true;
        return item.hasExtension(WeaponRune.class);
    }

    private final boolean fundamental;
    private final int grantsProperty;

    Rune(Rune.Builder builder, Item baseItem) {
        super(baseItem);
        fundamental = builder.fundamental;
        grantsProperty = builder.grantsProperty;
    }

    public String getBaseRune() {
        int end = getItem().getName().indexOf("(");
        if(end == -1)
            end = getItem().getName().length();
        return getItem().getName().substring(0, end).trim();
    }

    public boolean isFundamental() {
        return fundamental;
    }

    public int getGrantsProperty() {
        return grantsProperty;
    }

    public String getTier() {
        return StringUtils.getInBrackets(getItem().getName());
    }

    public static abstract class Builder extends ItemExtension.Builder {
        private boolean fundamental = false;
        private int grantsProperty = 0;

        public void setFundamental(boolean fundamental) {
            this.fundamental = fundamental;
        }

        public void setGrantsProperties(int grantsProperty) {
            this.grantsProperty = grantsProperty;
        }
    }
}
