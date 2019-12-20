package model.abilities.abilitySlots;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import model.abilities.Ability;

import java.util.Collections;
import java.util.List;

public class FeatSlot extends AbilitySlot implements AbilitySingleChoice {
    private final List<String> allowedTypes;

    public FeatSlot(String name, int level, List<String> allowedTypes) {
        super(name, level);
        this.allowedTypes = allowedTypes;
    }

    public List<String> getAllowedTypes() {
        return Collections.unmodifiableList(allowedTypes);
    }

    @Override
    public void fill(Ability choice) {
        if(currentAbility == null) currentAbility = new ReadOnlyObjectWrapper<>(choice);
        else if(currentAbility.get() != choice)
            currentAbility.set(choice);
    }

    @Override
    public Ability getChoice() {
        return (currentAbility == null) ? null : currentAbility.get();
    }

    @Override
    public ReadOnlyObjectProperty<Ability> getChoiceProperty() {
        if(currentAbility == null) currentAbility = new ReadOnlyObjectWrapper<>(null);
        return currentAbility.getReadOnlyProperty();
    }

    @Override
    public String toString() {
        return getName()+" "+getLevel();
    }

    @Override
    public void empty() {
        currentAbility =null;
    }

}
