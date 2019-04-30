package model.abilities.abilitySlots;

import model.abilities.Ability;
import model.enums.Type;

import java.util.Collections;
import java.util.List;

import static ui.Main.character;

public class FeatSlot extends AbilitySlot implements Pickable {
    private List<Type> allowedTypes;

    public FeatSlot(String name, int level, List<Type> allowedTypes) {
        super(name, level);
        this.allowedTypes = allowedTypes;
    }

    public List<Type> getAllowedTypes() {
        return Collections.unmodifiableList(allowedTypes);
    }

    @Override
    public List<Ability> getAbilities(int level) {
        return character.getFeatSet(allowedTypes, level);
    }

    @Override
    public void fill(Ability choice) {
        currentAbility = choice;
    }

}
