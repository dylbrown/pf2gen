package model.abc;

import model.ability_scores.AbilityMod;

import java.util.Collections;
import java.util.List;

public abstract class ABC {
    private final String name;
    private final List<AbilityMod> abilityMods;
    private final String description;

    ABC(ABC.Builder builder) {
        this.name = builder.name;
        this.description = builder.description;
        this.abilityMods = builder.abilityMods;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public String getName() {return this.name;}

    public List<AbilityMod> getAbilityMods() {
        return Collections.unmodifiableList(abilityMods);
    }

    public String getDesc() {
        return description;
    }

    public static abstract class Builder {
        private String name;
        private List<AbilityMod> abilityMods = Collections.emptyList();
        private String description;

        public void setName(String name) {
            this.name = name;
        }

        public void setAbilityMods(List<AbilityMod> abilityMods) {
            this.abilityMods = abilityMods;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
