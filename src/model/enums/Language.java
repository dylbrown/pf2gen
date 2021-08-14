package model.enums;

import model.AbstractNamedObject;

public class Language extends AbstractNamedObject implements Comparable<Language> {
    private final Rarity rarity;

    private Language(Builder builder) {
        super(builder);
        this.rarity = builder.rarity;
    }

    public Rarity getRarity() {
        return rarity;
    }

    @Override
    public int compareTo(Language o) {
        return this.getName().compareTo(o.getName());
    }

    public static class Builder extends AbstractNamedObject.Builder {
        public Rarity rarity;
        public Language build() {
            return new Language(this);
        }
    }
}
