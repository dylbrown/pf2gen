package model.abc;

import model.AttributeMod;
import model.ability_scores.AbilityMod;
import model.enums.Attribute;
import model.enums.Proficiency;

import java.util.List;

public class Background extends ABC {
    private final AttributeMod mod;
    private final String freeFeat;
    private final String modString;
    
    public Background(String name, String modString, String feat, String desc, List<AbilityMod> abilityMods, Attribute skill, String data) {
        super(name, desc, abilityMods);
        this.modString = modString;
        this.mod = new AttributeMod(skill, Proficiency.Trained, data);
        this.freeFeat = feat;
    }

    public AttributeMod getMod() {
        return mod;
    }

    public String getModString() {
        return modString;
    }
}
