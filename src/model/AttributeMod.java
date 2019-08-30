package model;

import model.enums.Attribute;
import model.enums.Proficiency;

import java.util.Objects;

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
        if(attr == null) return "";
        return (data != null && !data.equals("")) ? attr.toString()+" ("+data+")" : attr.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AttributeMod that = (AttributeMod) o;
        return Objects.equals(data, that.data) &&
                attr == that.attr &&
                mod == that.mod;
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, attr, mod);
    }
}
