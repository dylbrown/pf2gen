package model.abilities;

import java.util.HashMap;

public class ScalingExtension extends AbilityExtension {
    private HashMap<Integer, Ability> scaledAbilities = new HashMap<>();

    private ScalingExtension(ScalingExtension.Builder builder, Ability baseAbility) {
        super(baseAbility);
    }

    public Ability getAbility(int level) {
        if(level < getBaseAbility().getLevel() || level > 20)
            throw new IndexOutOfBoundsException("Specified level below minimum or above maximum");
        return scaledAbilities.computeIfAbsent(level, i-> getBaseAbility().copyWithLevel(i));
    }

    public static class Builder extends AbilityExtension.Builder {
        @Override
        public ScalingExtension build(Ability baseAbility) {
            return new ScalingExtension(this, baseAbility);
        }
    }
}
