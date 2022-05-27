package model.abilities;

public class CustomTextExtension extends AbilityExtension {
    private final String customName;

    private CustomTextExtension(CustomTextExtension.Builder builder, Ability baseAbility) {
        super(baseAbility);
        customName = builder.customName;
    }

    public String getCustomName() {
        return customName;
    }

    public static class Builder extends AbilityExtension.Builder {
        private  String customName;
        public Builder() {}

        public Builder(CustomTextExtension.Builder other) {
            customName = other.customName;
        }

        public void setCustomName(String customName) {
            this.customName = customName;
        }

        @Override
        CustomTextExtension build(Ability baseAbility) {
            return new CustomTextExtension(this, baseAbility);
        }
    }
}
