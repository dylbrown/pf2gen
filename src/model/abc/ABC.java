package model.abc;

import model.AbstractNamedObject;
import model.ability_scores.AbilityMod;
import model.data_managers.sources.Source;

import java.util.Collections;
import java.util.List;

public abstract class ABC extends AbstractNamedObject {
    private final List<AbilityMod> abilityMods;

    ABC(ABC.Builder builder) {
        super(builder);
        this.abilityMods = builder.abilityMods;
    }

    public List<AbilityMod> getAbilityMods() {
        return Collections.unmodifiableList(abilityMods);
    }

    public static abstract class Builder extends AbstractNamedObject.Builder {
        private List<AbilityMod> abilityMods = Collections.emptyList();

        protected Builder(Source source) {
            super(source);
        }

        public void setAbilityMods(List<AbilityMod> abilityMods) {
            this.abilityMods = abilityMods;
        }
    }
}
