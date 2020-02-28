package model.equipment;

public class SpecialCustomTrait extends CustomTrait {
    private final String specialText;

    public SpecialCustomTrait(String name, String effect, String specialText) {
        super(name, effect);
        this.specialText = specialText;
    }

    public SpecialCustomTrait(String name, String specialText) {
        super(name);
        this.specialText = specialText;
    }

    public SpecialCustomTrait(CustomTrait customTrait, String specialText) {
        super(customTrait.getName(), customTrait.getEffect());
        this.specialText = specialText;
    }

    @Override
    public String getName() {
        return super.getName() + " " + specialText;
    }

    @Override
    public String toString() {
        return super.toString() + " " + specialText;
    }
}
