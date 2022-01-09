package model.enums;

import model.AbstractNamedObject;
import model.data_managers.sources.Source;

public class Sense extends AbstractNamedObject implements Comparable<Sense> {

    protected Sense(Builder builder) {
        super(builder);
    }

    @Override
    public int compareTo(Sense o) {
        return getName().compareToIgnoreCase(o.getName());
    }

    public static class Builder extends AbstractNamedObject.Builder {
        public Builder(Source source) {
            super(source);
        }

        public Sense build() {
            return new Sense(this);
        }
    }
}
