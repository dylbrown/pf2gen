package model.abilities.abilitySlots;

import model.abilities.Ability;

import java.util.Collections;
import java.util.List;

public class ChoiceSlot extends AbilitySlot implements Choice<Ability> {
    private final List<Ability> choices;

    public ChoiceSlot(String abilityName, int level, List<Ability> choices) {
        super(abilityName, level);
        this.choices = choices;
    }


    private List<Ability> getAbilities() {
        return Collections.unmodifiableList(choices);
    }

    @Override
    public List<Ability> getOptions() {
        return getAbilities();
    }

    @Override
    public void fill(Ability choice) {
        if(choices.contains(choice))
            currentAbility = choice;
    }

    @Override
    public Ability getChoice() {
        return currentAbility;
    }

    @Override
    public void empty() {
        currentAbility =null;
    }

}
