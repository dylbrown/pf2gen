package model.ability_scores;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import model.enums.Type;

import java.util.Objects;

public class AbilityMod {
    final ReadOnlyObjectWrapper<AbilityScore> target;
    private final boolean positive;

    public Type getType() {
        return type;
    }

    private final Type type;

    public AbilityMod(AbilityScore target, boolean positive, Type type) {
        this.target = new ReadOnlyObjectWrapper<>(target);
        this.positive = positive;
        this.type = type;
    }

    public boolean isPositive() {
        return positive;
    }

    public AbilityScore getTarget() {
        return target.get();
    }

    public ReadOnlyObjectProperty<AbilityScore> getTargetProperty() {
        return target.getReadOnlyProperty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbilityMod that = (AbilityMod) o;
        return positive == that.positive &&
                target == that.target &&
                type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(target, positive, type);
    }
}
