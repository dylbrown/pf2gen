package model.abilities.abilitySlots;

import model.abilities.Ability;
import model.enums.Type;

import java.util.Collections;
import java.util.List;

import static ui.Main.character;

public class FeatSlot extends AbilitySlot implements Pickable {
    private List<Type> allowedTypes;

    public FeatSlot(String name, List<Type> allowedTypes) {
        super(name);
        this.allowedTypes = allowedTypes;
    }

    public List<Type> getAllowedTypes() {
        return Collections.unmodifiableList(allowedTypes);
    }

    @Override
    public List<Ability> getAbilities() {
        return character.getFeatSet(allowedTypes);
    }

    @Override
    public void fill(Ability choice) {
        currentAbility = choice;
    }

}
