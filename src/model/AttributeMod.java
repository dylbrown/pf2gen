package model;

import model.enums.Attribute;
import model.enums.Proficiency;

public class AttributeMod {
    private String data;
    private final Attribute attr;
    private final Proficiency mod;
    public AttributeMod(Attribute attr, Proficiency mod) {
        this.attr = attr;
        this.mod = mod;
    }

    public AttributeMod(Attribute attr, Proficiency mod, String data) {
        this(attr, mod);
        this.data = data;
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

    public String toNiceAttributeString() {
        return (data != null) ? attr.toString()+" ("+data+")" : attr.toString();
    }
}
