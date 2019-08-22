package model.abilities.abilitySlots;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import model.abilities.Ability;

import java.util.Collections;
import java.util.List;

public class ChoiceSlot extends AbilitySlot implements ChoiceList<Ability>, AbilityChoice {
    private final List<Ability> choices;

    public ChoiceSlot(String abilityName, int level, List<Ability> choices) {
        super(abilityName, level);
        this.choices = choices;
    }

    @Override
    public List<Ability> getOptions() {
        return Collections.unmodifiableList(choices);
    }

    @Override
    public void fill(Ability choice) {
        if(choices.contains(choice)){
            if(currentAbility == null)
                currentAbility = new ReadOnlyObjectWrapper<>(choice);
            else
                currentAbility.set(choice);
        }
    }

    @Override
    public Ability getChoice() {
        return currentAbility.get();
    }

    @Override
    public ReadOnlyObjectProperty<Ability> getChoiceProperty() {
        return currentAbility.getReadOnlyProperty();
    }

    @Override
    public void empty() {
        currentAbility =null;
    }

}
