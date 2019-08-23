package model.equipment;

public class SpecialItemTrait extends ItemTrait {
    private final String specialText;

    public SpecialItemTrait(String name, String effect, String specialText) {
        super(name, effect);
        this.specialText = specialText;
    }

    public SpecialItemTrait(String name, String specialText) {
        super(name);
        this.specialText = specialText;
    }

    public SpecialItemTrait(ItemTrait itemTrait, String specialText) {
        super(itemTrait.getName(), itemTrait.getEffect());
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
