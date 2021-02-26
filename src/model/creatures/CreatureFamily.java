package model.creatures;

import model.AbstractNamedObject;

public class CreatureFamily extends AbstractNamedObject {
    protected CreatureFamily(Builder builder) {
        super(builder);
    }

    public static class Builder extends AbstractNamedObject.Builder {
        public CreatureFamily build() {
            return new CreatureFamily(this);
        }
    }
}
