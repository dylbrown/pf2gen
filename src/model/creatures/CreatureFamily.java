package model.creatures;

import model.NamedObject;

public class CreatureFamily extends NamedObject {
    protected CreatureFamily(Builder builder) {
        super(builder);
    }

    public static class Builder extends NamedObject.Builder {
        public CreatureFamily build() {
            return new CreatureFamily(this);
        }
    }
}
