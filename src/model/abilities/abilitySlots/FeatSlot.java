package model.abilities.abilitySlots;

import javafx.beans.property.ReadOnlyObjectProperty;
import model.abilities.Ability;
import model.enums.Type;

import java.util.Collections;
import java.util.List;

public class FeatSlot extends AbilitySlot implements AbilityChoice {
    private final List<Type> allowedTypes;

    public FeatSlot(String name, int level, List<Type> allowedTypes) {
        super(name, level);
        this.allowedTypes = allowedTypes;
    }

    public List<Type> getAllowedTypes() {
        return Collections.unmodifiableList(allowedTypes);
    }

    @Override
    public void fill(Ability choice) {
        currentAbility.set(choice);
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
