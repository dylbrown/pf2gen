package model.abilities.abilitySlots;

import model.abilities.Ability;

import java.util.Collections;
import java.util.List;

public class ChoiceSlot extends AbilitySlot implements Pickable {
    private final List<Ability> choices;

    public ChoiceSlot(String abilityName, int level, List<Ability> choices) {
        super(abilityName, level);
        this.choices = choices;
    }

    @Override
    public List<Ability> getAbilities(int level) {
        return getAbilities();
    }

    private List<Ability> getAbilities() {
        return Collections.unmodifiableList(choices);
    }

    @Override
    public void fill(Ability choice) {
        if(choices.contains(choice))
            currentAbility = choice;
    }
}
