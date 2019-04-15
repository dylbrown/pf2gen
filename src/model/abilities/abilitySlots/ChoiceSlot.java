package model.abilities.abilitySlots;

import model.abilities.Ability;

import java.util.List;

public class ChoiceSlot extends AbilitySlot {
    private List<Ability> choices;
    public ChoiceSlot(String abilityName, List<Ability> choices) {
        super(abilityName);
        this.choices = choices;
    }
}
