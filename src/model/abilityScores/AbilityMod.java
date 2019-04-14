package model.abilityScores;

import model.enums.AbilityType;

import java.util.Objects;

public class AbilityMod {
    private AbilityScore target;
    private boolean positive;
    private AbilityType source;
    private int level = 1;

    public AbilityMod(AbilityScore target, boolean positive, AbilityType source) {
        this.target = target;
        this.positive = positive;
        this.source = source;
    }

    public boolean isPositive() {
        return positive;
    }

    public AbilityScore getTarget() {
        return target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbilityMod that = (AbilityMod) o;
        return positive == that.positive &&
                level == that.level &&
                target == that.target &&
                source == that.source;
    }

    @Override
    public int hashCode() {
        return Objects.hash(target, positive, source, level);
    }
}
