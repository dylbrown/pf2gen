package model.enums;

import model.AbstractNamedObject;

public class Trait extends AbstractNamedObject implements Comparable<Trait> {
    private final String category;

    protected Trait(Builder builder) {
        super(builder);
        category = builder.category;
    }

    public String getCategory() {
        return category;
    }

    @Override
    public int compareTo(Trait o) {
        return getName().compareToIgnoreCase(o.getName());
    }

    public static class Builder extends AbstractNamedObject.Builder {
        private String category = "";

        public void setCategory(String category) {
            this.category = category;
        }

        public Trait build() {
            return new Trait(this);
        }
    }
}
