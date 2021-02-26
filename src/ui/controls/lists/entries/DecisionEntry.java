package ui.controls.lists.entries;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import model.ability_slots.Choice;
import model.util.StringUtils;


public class DecisionEntry extends ListEntry<Choice<?>> {
    private final ReadOnlyStringWrapper level;
    private final ReadOnlyStringProperty levelReadOnly;
    private Object chosenValue;
    private ReadOnlyIntegerProperty remaining;
    private ReadOnlyIntegerWrapper remainingWrapper;
    private static final ReadOnlyStringProperty empty = new ReadOnlyStringWrapper("").getReadOnlyProperty();

    public <T> DecisionEntry(Choice<T> choice) {
        this(choice, choice.toString());
        remainingWrapper = new ReadOnlyIntegerWrapper(choice.getMaxSelections());
        remaining = remainingWrapper.getReadOnlyProperty();
        remainingWrapper.bind(choice.maxSelectionsProperty().subtract(choice.getSelections().size()));
        choice.getSelections().addListener((ListChangeListener<T>) c->
                remainingWrapper.bind(choice.maxSelectionsProperty().subtract(choice.getSelections().size())));
    }

    private <T> DecisionEntry(Choice<T> choice, String name) {
        super(choice, name);
        chosenValue = null;
        level = new ReadOnlyStringWrapper(String.valueOf(choice.getLevel()));
        levelReadOnly = level.getReadOnlyProperty();
    }

    public <T> DecisionEntry(Choice<T> choice, T chosenValue) {
        this(choice, chosenValue.toString());
        this.chosenValue = chosenValue;
        this.level.set("");
    }

    public DecisionEntry(String name) {
        super(name);
        level = new ReadOnlyStringWrapper("");
        levelReadOnly = level.getReadOnlyProperty();
        chosenValue = null;
    }

    public Object getChosenValue() {
        return chosenValue;
    }

    @Override
    public ObservableValue<String> get(String propertyName) {
        switch (propertyName) {
            case "name": return nameProperty();
            case "level": return levelReadOnly;
            case "remaining": {
                if(remaining != null) return remaining.asString();
                else return empty;
            }
            default: return null;
        }
    }

    @Override
    public String toString() {
        if(chosenValue != null)
            return chosenValue.toString();
        return super.toString();
    }

    public Choice<?> getChoice() {
        return getContents();
    }

    @Override
    public int compareTo(ListEntry<Choice<?>> o) {
        String s1 = this.toString();
        String s2 = o.toString();
        boolean level1 = s1.matches(".* \\d+\\z");
        boolean level2 = s2.matches(".* \\d+\\z");
        if(level1)
            s1 = s1.substring(s1.lastIndexOf(" ") + 1);
        if(level2)
            s2 = s2.substring(s2.lastIndexOf(" ") + 1);
        if(level1 && level2)
            return Integer.compare(
                    Integer.parseInt(s1),
                    Integer.parseInt(s2));
        else if(level2 && this.getContents() != null)
            return Integer.compare(
                    this.getContents().getLevel(),
                    Integer.parseInt(s2));
        else if(level1 && o.getContents() != null)
            return Integer.compare(
                    Integer.parseInt(s1),
                    o.getContents().getLevel());
        else return StringUtils.clean(s1).compareTo(StringUtils.clean(s2));
    }
}
