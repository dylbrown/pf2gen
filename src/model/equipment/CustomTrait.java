package model.equipment;

import model.enums.Trait;

public class CustomTrait extends Trait {
    private final String specialText;
    private final Trait trait;

    public CustomTrait(Builder builder) {
        super(builder);
        specialText = builder.specialText;
        trait = builder.trait;
    }

    public CustomTrait(Trait trait, String custom) {
        super(new Builder(trait));
        this.trait = trait;
        this.specialText = custom;
    }

    public String getSpecialText() {
        return specialText;
    }

    public Trait getTrait() {
        return trait;
    }

    @Override
    public String getName() {
        return super.getName() + " " + specialText;
    }

    @Override
    public String toString() {
        return super.toString() + " " + specialText;
    }

    public static class Builder extends Trait.Builder {
        private String specialText;
        private final Trait trait;

        public Builder(Trait trait) {
            this.trait = trait;
            setName(trait.getName());
            setCategory(trait.getCategory());
            setDescription(trait.getDescription());
            setPage(trait.getPage());
            setSourceBook(trait.getSourceBook());
        }

        public void setSpecialText(String specialText) {
            this.specialText = specialText;
        }

        public CustomTrait build() {
            return new CustomTrait(this);
        }
    }
}
