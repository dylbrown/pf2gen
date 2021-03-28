package model.attributes;

import model.ability_scores.AbilityScore;

import java.util.Objects;

public class CustomAttribute implements Attribute {

    private final BaseAttribute attribute;
    private final String data;

    public static Attribute get(BaseAttribute attribute, String data) {
        if(data == null || data.isBlank())
            return attribute;
        return new CustomAttribute(attribute, data);
    }

    private CustomAttribute(BaseAttribute attribute, String data) {
        this.attribute = attribute;
        this.data = data;
    }

    public BaseAttribute getBase() {
        return attribute;
    }

    @Override
    public AbilityScore getKeyAbility() {
        return attribute.getKeyAbility();
    }

    @Override
    public boolean hasACP() {
        return attribute.hasACP();
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
