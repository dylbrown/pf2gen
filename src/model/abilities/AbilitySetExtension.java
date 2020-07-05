package model.abilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AbilitySetExtension extends AbilityExtension {
    private final List<Ability> abilities;
    private AbilitySetExtension(AbilitySetExtension.Builder builder, Ability baseAbility) {
        super(baseAbility);
        this.abilities = builder.abilities;
    }

    public List<Ability> getAbilities(){
        return Collections.unmodifiableList(abilities);
    }

    public static class Builder extends AbilityExtension.Builder {
        private List<Ability> abilities;

        Builder() {}

        public Builder(Builder other) {
            this.abilities = new ArrayList<>(other.abilities);
        }

        public void setAbilities(List<Ability> abilities) {
            this.abilities = abilities;
        }

        @Override
        public AbilitySetExtension build(Ability baseAbility) {
            return new AbilitySetExtension(this, baseAbility);
        }
    }
}
