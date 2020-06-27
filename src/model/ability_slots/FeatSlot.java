package model.ability_slots;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.abilities.Ability;

import java.util.Collections;
import java.util.List;

public class FeatSlot extends AbilitySlot implements AbilitySingleChoice {
    private final List<String> allowedTypes;
    private final ObservableList<Ability> list = FXCollections.observableArrayList();

    public FeatSlot(String name, int level, List<String> allowedTypes) {
        super(name, level);
        this.allowedTypes = allowedTypes;
    }

    public List<String> getAllowedTypes() {
        return Collections.unmodifiableList(allowedTypes);
    }

    public void fill(Ability choice) {
        if(currentAbility == null) currentAbility = new ReadOnlyObjectWrapper<>(choice);
        else if(currentAbility.get() != choice) {
            currentAbility.set(choice);
            list.clear();
            if(choice != null)
                list.add(choice);
        }
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
    public Class<Ability> getOptionsClass() {
        return Ability.class;
    }

    @Override
    public void add(Ability choice) {
        if(list.size() == 0) fill(choice);
    }

    @Override
    public void remove(Ability choice) {
        if(list.size() == 1 && currentAbility.get().equals(choice)) fill(null);
    }

    @Override
    public void empty() {
        if(list.size() == 0) fill(null);
    }

    private final ObservableList<Ability> unmodifiable = FXCollections.unmodifiableObservableList(list);
    @Override
    public ObservableList<Ability> getSelections() {
        return unmodifiable;
    }

}
