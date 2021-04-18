package model.creatures;

import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import model.attributes.Attribute;

public class CustomCreatureAttribute {
    private final Attribute attribute;
    public final ReadOnlyIntegerWrapper modifier = new ReadOnlyIntegerWrapper(0);
    public final ReadOnlyStringWrapper info = new ReadOnlyStringWrapper("");
    private final CreatureAttribute creatureAttribute;

    public CustomCreatureAttribute(Attribute a) {
        this.attribute = a;
        this.creatureAttribute = new UnmodifiableCustomCreatureAttribute();
    }

    public int getModifier() {
        return modifier.get();
    }

    public CreatureAttribute getAsCreatureAttribute() {
        return creatureAttribute;
    }

    private class UnmodifiableCustomCreatureAttribute implements CreatureAttribute {
        public Attribute getAttribute() {
            return attribute;
        }

        @Override
        public int getModifier() {
            return modifier.get();
        }

        @Override
        public String getInfo() {
            return info.get();
        }
    }
}
