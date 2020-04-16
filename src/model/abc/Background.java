package model.abc;

import model.abilities.abilitySlots.AbilitySlot;
import model.abilities.abilitySlots.DynamicFilledSlot;
import model.attributes.AttributeMod;
import model.enums.Type;

import java.util.Arrays;
import java.util.List;

public class Background extends ABC {
    private final AttributeMod mod1;
    private final AttributeMod mod2;
    private final AbilitySlot freeFeat;
    private final String modString;
    
    private Background(Background.Builder builder) {
        super(builder);
        this.modString = builder.modString;
        this.mod1 = builder.mod1;
        this.mod2 = builder.mod2;
        this.freeFeat = new DynamicFilledSlot("Background Feat", 1, builder.feat, Type.Skill, false);
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

    public static class Builder extends ABC.Builder {
        private AttributeMod mod1;
        private AttributeMod mod2;
        private String feat;
        private String modString;

        public void setMod1(AttributeMod mod1) {
            this.mod1 = mod1;
        }

        public void setMod2(AttributeMod mod2) {
            this.mod2 = mod2;
        }

        public void setFeat(String feat) {
            this.feat = feat;
        }

        public void setModString(String modString) {
            this.modString = modString;
        }

        public Background build() {
            return new Background(this);
        }
    }
}
