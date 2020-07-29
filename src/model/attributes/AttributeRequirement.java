package model.attributes;

import model.enums.Proficiency;

import java.util.Collections;
import java.util.List;

public class AttributeRequirement {
    private final Attribute attr;
    private final Proficiency mod;
    private final String data;

    public AttributeRequirement(Attribute attr, Proficiency mod, String data) {
        this.attr = attr;
        this.mod = mod;
        this.data = data;
    }

    AttributeRequirement () {
        attr = Attribute.None;
        mod = Proficiency.Untrained;
        data = null;
    }

    public Attribute getAttr() {
        return attr;
    }

    public Proficiency getMod() {
        return mod;
    }

    public String getData() {
        return data;
    }

    public List<AttributeRequirement> requirements() {
        return Collections.singletonList(this);
    }
}
