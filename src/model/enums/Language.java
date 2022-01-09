package model.enums;

import model.AbstractNamedObject;
import model.data_managers.sources.Source;

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

        public Builder(Source source) {
            super(source);
        }

        public Language build() {
            return new Language(this);
        }
    }
}
