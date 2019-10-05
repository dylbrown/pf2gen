package model.abc;

import model.AttributeMod;
import model.abilities.abilitySlots.AbilitySlot;
import model.abilities.abilitySlots.DynamicFilledSlot;
import model.ability_scores.AbilityMod;
import model.enums.Type;

import java.util.Arrays;
import java.util.List;

public class Background extends ABC {
    private final AttributeMod mod1;
    private final AttributeMod mod2;
    private final AbilitySlot freeFeat;
    private final String modString;
    
    public Background(String name, String modString, String feat, String desc, List<AbilityMod> abilityMods, AttributeMod mod1, AttributeMod mod2) {
        super(name, desc, abilityMods);
        this.modString = modString;
        this.mod1 = mod1;
        this.mod2 = mod2;
        this.freeFeat = new DynamicFilledSlot("Background Feat", 1, feat, Type.Skill, false);
    }

    public List<AttributeMod> getMods() {
        return Arrays.asList(mod1, mod2);
    }

    public String getModString() {
        return modString;
    }

    public AbilitySlot getFreeFeat() {
        return freeFeat;
    }
}
