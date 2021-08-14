package model.attributes;

import model.enums.Proficiency;

import java.util.Objects;

public class AttributeMod {
    public static final AttributeMod NONE = new AttributeMod(BaseAttribute.None, Proficiency.Untrained);
    private final Attribute attr;
    private final Proficiency mod;
    public AttributeMod(Attribute attr, Proficiency mod) {
        this.attr = attr;
        this.mod = mod;
    }

    public Attribute getAttr() {
        return attr;
    }

    public Proficiency getMod() {
        return mod;
    }

    public String toNiceAttributeString() {
        if(attr == null) return "";
        return attr.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AttributeMod that = (AttributeMod) o;
        return attr == that.attr &&
                mod == that.mod;
    }

    @Override
    public int hashCode() {
        return Objects.hash(attr, mod);
    }
}
