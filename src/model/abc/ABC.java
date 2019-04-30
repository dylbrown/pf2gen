package model.abc;

import model.abilityScores.AbilityMod;

import java.util.Collections;
import java.util.List;

public class ABC {
    private final String name;
    private final List<AbilityMod> abilityMods;
    private String description;

    public ABC(String name, String description, List<AbilityMod> abilityMods) {
        this.name = name;
        this.description = description;
        this.abilityMods = abilityMods;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public List<AbilityMod> getAbilityMods() {
        return Collections.unmodifiableList(abilityMods);
    }

    public String getDesc() {
        return description;
    }
}
