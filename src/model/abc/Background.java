package model.abc;

import model.AttributeMod;
import model.abilities.abilitySlots.AbilitySlot;
import model.abilities.abilitySlots.DynamicFilledSlot;
import model.ability_scores.AbilityMod;
import model.enums.Attribute;
import model.enums.Proficiency;
import model.enums.Type;

import java.util.Arrays;
import java.util.List;

public class Background extends ABC {
    private final AttributeMod mod1;
    private final AttributeMod mod2;
    private final AbilitySlot freeFeat;
    private final String modString;
    
    public Background(String name, String modString, String feat, String desc, List<AbilityMod> abilityMods, Attribute skill1, Attribute skill2, String data1, String data2) {
        super(name, desc, abilityMods);
        this.modString = modString;
        this.mod1 = new AttributeMod(skill1, Proficiency.Trained, data1);
        this.mod2 = new AttributeMod(skill2, Proficiency.Trained, data2);
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
