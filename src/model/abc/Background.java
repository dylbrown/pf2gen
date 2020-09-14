package model.abc;

import model.abilities.Ability;
import model.ability_slots.AbilitySlot;
import model.ability_slots.DynamicFilledSlot;
import model.attributes.AttributeMod;
import model.enums.Type;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class Background extends ABC {
    public static final Background NO_BACKGROUND;
    private final AttributeMod mod1;
    private final AttributeMod mod2;
    private final AbilitySlot freeFeat;
    private final String modString;

    static{
        Builder builder = new Builder();
        builder.setName("No Background");
        NO_BACKGROUND = builder.build();
    }
    
    private Background(Background.Builder builder) {
        super(builder);
        this.modString = builder.modString;
        this.mod1 = builder.mod1;
        this.mod2 = builder.mod2;
        this.freeFeat = new DynamicFilledSlot("Background Feat", 1, builder.feat, Type.Skill, (type, name)->builder.abilityFunction.apply(name));
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
        private AttributeMod mod1 = AttributeMod.NONE;
        private AttributeMod mod2 = AttributeMod.NONE;
        private String feat = "";
        private String modString;
        private Function<String, Ability> abilityFunction;

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

        public void setAbilityFunction(Function<String, Ability> abilityFunction) {
            this.abilityFunction = abilityFunction;
        }

        public Background build() {
            return new Background(this);
        }
    }
}
