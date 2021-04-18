package model.creatures;

import model.attributes.Attribute;

public class BaseCreatureAttribute implements CreatureAttribute {
    private final Attribute attribute;
    private final int modifier;
    private final String info;

    private BaseCreatureAttribute(Builder builder) {
        this.attribute = builder.attribute;
        this.modifier = builder.modifier;
        this.info = builder.info;
    }

    @Override
    public Attribute getAttribute() {
        return attribute;
    }

    @Override
    public int getModifier() {
        return modifier;
    }

    @Override
    public String getInfo() {
        return info;
    }

    public static class Builder {
        private final Attribute attribute;
        public int modifier;
        public String info;

        public Builder(Attribute attribute) {
            this.attribute = attribute;
        }

        public Attribute getAttribute() {
            return attribute;
        }

        public BaseCreatureAttribute build() {
            return new BaseCreatureAttribute(this);
        }
    }
}
