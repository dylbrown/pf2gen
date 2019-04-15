package model.abilities.abilitySlots;

import model.enums.Type;

import java.util.List;

public class FeatSlot extends AbilitySlot {
    private List<Type> allowedTypes;
    public FeatSlot(String name, List<Type> allowedTypes) {
        super(name);
        this.allowedTypes = allowedTypes;
    }
}
