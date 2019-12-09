package model.abilities;

import java.util.Collections;
import java.util.List;

public class AbilitySet extends Ability {
    private final List<Ability> abilities;
    private AbilitySet(AbilitySet.Builder builder) {
        super(builder);
        this.abilities = builder.abilities;
    }

    public List<Ability> getAbilities(){
        return Collections.unmodifiableList(abilities);
    }

    public static class Builder extends Ability.Builder {
        private List<Ability> abilities;

	    public Builder(Ability.Builder builder) {
            super(builder);
        }

	    public void setAbilities(List<Ability> abilities) {
            this.abilities = abilities;
        }

        @Override
        public AbilitySet build() {
            return new AbilitySet(this);
        }
    }
}
