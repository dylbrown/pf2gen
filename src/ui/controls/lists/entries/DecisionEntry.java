package ui.controls.lists.entries;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import model.ability_slots.Choice;

@SuppressWarnings("rawtypes")
public class DecisionEntry implements Comparable<DecisionEntry>, TreeTableEntry {
    private final ReadOnlyStringProperty name;
    private final ReadOnlyStringProperty level;
    private final Object chosenValue;
    private ReadOnlyIntegerProperty remaining;
    private ReadOnlyIntegerWrapper remainingWrapper;
    private static final ReadOnlyStringProperty empty = new ReadOnlyStringWrapper("").getReadOnlyProperty();
    private final Choice<?> choice;

    public DecisionEntry(Choice<?> choice) {
        this.choice = choice;
        chosenValue = null;
        name = new ReadOnlyStringWrapper(choice.getName()).getReadOnlyProperty();
        level = new ReadOnlyStringWrapper(String.valueOf(choice.getLevel())).getReadOnlyProperty();
        remainingWrapper = new ReadOnlyIntegerWrapper(choice.getNumSelections());
        remaining = remainingWrapper.getReadOnlyProperty();
        remainingWrapper.bind(choice.numSelectionsProperty().subtract(choice.getSelections().size()));
        //noinspection unchecked
        choice.getSelections().addListener((ListChangeListener) c->
                remainingWrapper.bind(choice.numSelectionsProperty().subtract(choice.getSelections().size())));
    }

    public DecisionEntry(Object chosenValue, String name, int level) {
        this.choice = null;
        this.chosenValue = chosenValue;
        this.name = new ReadOnlyStringWrapper(name).getReadOnlyProperty();
        this.level = new ReadOnlyStringWrapper((level == -1) ? "" : String.valueOf(level)).getReadOnlyProperty();
    }

    @Override
    public int compareTo(DecisionEntry o) {
        return name.get().compareTo(o.name.get());
    }

    public Object getChosenValue() {
        return chosenValue;
    }

    @Override
    public ObservableValue<String> get(String propertyName) {
        switch (propertyName) {
            case "name": return name;
            case "level": return level;
            case "remaining": {
                if(remaining != null) return remaining.asString();
                else return empty;
            }
            default: return null;
        }
    }

    public String getName() {
        return name.get();
    }

    public Choice<?> getChoice() {
        return choice;
    }

    @Override
    public String toString() {
        return getName();
    }
}
