package ui.controls.lists.entries;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;

public class CreatureCountEntry extends ListEntry<CreatureCount> {
    private final ReadOnlyStringWrapper level;
    private final ObservableValue<String> countProperty;

    public CreatureCountEntry(CreatureCount item) {
        super(item, item.getCreature().getName());
        level = new ReadOnlyStringWrapper(String.valueOf(item.getCreature().getLevel()));
        countProperty = item.countProperty().asString();
    }

    public CreatureCountEntry(String label) {
        super(label);
        level = new ReadOnlyStringWrapper("");
        countProperty = new ReadOnlyStringWrapper("").getReadOnlyProperty();
    }

    @Override
    public ObservableValue<String> get(String propertyName) {
        switch (propertyName) {
            case "name":
                return nameProperty();
            case "level":
                return levelProperty();
            case "count":
                return countProperty;
        }
        return null;
    }

    public ReadOnlyStringProperty levelProperty() {
        return level.getReadOnlyProperty();
    }
}
