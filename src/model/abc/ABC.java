package model.abc;

import model.NamedObject;
import model.ability_scores.AbilityMod;

import java.util.Collections;
import java.util.List;

public abstract class ABC extends NamedObject {
    private final List<AbilityMod> abilityMods;

    ABC(ABC.Builder builder) {
        super(builder);
        this.abilityMods = builder.abilityMods;
    }

    public List<AbilityMod> getAbilityMods() {
        return Collections.unmodifiableList(abilityMods);
    }

    public static abstract class Builder extends NamedObject.Builder {
        private List<AbilityMod> abilityMods = Collections.emptyList();

        protected Builder(){}

        public void setAbilityMods(List<AbilityMod> abilityMods) {
            this.abilityMods = abilityMods;
        }
    }
}
