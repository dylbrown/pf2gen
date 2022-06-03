package model.abilities;

import model.enums.Recalculate;

public class CustomModExtension extends AbilityExtension {
    private final String customMod;
    private final Recalculate recalculate;
    private final int minScriptVersion;

    private CustomModExtension(CustomModExtension.Builder builder, Ability baseAbility) {
        super(baseAbility);
        customMod = builder.customMod;
        this.recalculate = builder.recalculate;
        minScriptVersion = builder.minScriptVersion;
    }

    public String getCustomMod() {
        return customMod;
    }

    public Recalculate getRecalculate() {
        return recalculate;
    }

    public int getMinScriptVersion() {
        return minScriptVersion;
    }

    public static class Builder extends AbilityExtension.Builder {
        private  String customMod;
        private Recalculate recalculate = Recalculate.Never;
        private int minScriptVersion = 0;
        public Builder() {}

        public Builder(CustomModExtension.Builder other) {
            customMod = other.customMod;
            recalculate = other.recalculate;
            minScriptVersion = other.minScriptVersion;
        }

        public void setCustomMod(String customMod) {
            this.customMod = customMod;
        }

        public void setRecalculate(Recalculate recalculate) {
            this.recalculate = recalculate;
        }

        public void setMinScriptVersion(int minScriptVersion) {
            this.minScriptVersion = minScriptVersion;
        }

        @Override
        CustomModExtension build(Ability baseAbility) {
            return new CustomModExtension(this, baseAbility);
        }
    }
}
