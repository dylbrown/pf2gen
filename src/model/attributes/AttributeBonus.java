package model.attributes;

import model.enums.Type;

public class AttributeBonus {
    private final Attribute target;
    private final int bonus;
    private final Type source;

    public AttributeBonus(Attribute target, int bonus, Type source) {
        this.target = target;
        this.bonus = bonus;
        this.source = source;
    }

    public Attribute getTarget() {
        return target;
    }

    public int getBonus() {
        return bonus;
    }

    public Type getSource() {
        return source;
    }
}
