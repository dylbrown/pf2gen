package model.creatures;

import model.AbstractNamedObject;
import model.data_managers.sources.Source;

public class CreatureFamily extends AbstractNamedObject {
    protected CreatureFamily(Builder builder) {
        super(builder);
    }

    public static class Builder extends AbstractNamedObject.Builder {
        public Builder(Source source) {
            super(source);
        }

        public CreatureFamily build() {
            return new CreatureFamily(this);
        }
    }
}
