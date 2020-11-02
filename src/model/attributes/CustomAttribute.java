package model.attributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CustomAttribute {
    private static final Map<Attribute, CustomAttribute> map = new HashMap<>();

    public static CustomAttribute get(Attribute attribute) {
        return map.computeIfAbsent(attribute, CustomAttribute::new);
    }

    private final Attribute attribute;
    private final String data;

    public CustomAttribute(Attribute attribute, String data) {
        this.attribute = attribute;
        this.data = data;
    }

    private CustomAttribute(Attribute attribute) {
        this.attribute = attribute;
        data = null;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public String getData() {
        return data;
    }

    @Override
    public String toString() {
        return (data == null) ? attribute.toString() : attribute.toString() + " (" + data + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomAttribute that = (CustomAttribute) o;
        return attribute == that.attribute &&
                Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attribute, data);
    }
}
