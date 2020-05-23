package model.abilities.abilitySlots;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.abilities.Ability;

import java.util.Collections;
import java.util.List;

public class SingleChoiceSlot extends AbilitySlot implements AbilityChoiceList, AbilitySingleChoice {
    private final List<Ability> choices;
    private final ObservableList<Ability> list = FXCollections.observableArrayList();

    public SingleChoiceSlot(String abilityName, int level, List<Ability> choices) {
        super(abilityName, level);
        this.choices = choices;
    }

    @Override
    public List<Ability> getOptions() {
        return Collections.unmodifiableList(choices);
    }

    @Override
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
        return currentAbility == null ? null : currentAbility.get();
    }

    @Override
    public ReadOnlyObjectProperty<Ability> getChoiceProperty() {
        return currentAbility.getReadOnlyProperty();
    }

    @Override
    public void empty() {
        currentAbility =null;
    }

    @Override
    public void add(Ability choice) {
        if(list.size() == 0) fill(choice);
    }

    @Override
    public void remove(Ability choice) {
        if(list.size() == 1 && currentAbility.get().equals(choice)) fill(null);
    }

    private ObservableList<Ability> unmodifiable = FXCollections.unmodifiableObservableList(list);
    @Override
    public ObservableList<Ability> getSelections() {
        return unmodifiable;
    }
}
