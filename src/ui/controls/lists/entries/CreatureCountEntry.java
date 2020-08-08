package ui.controls.lists.entries;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;

public class CreatureCountEntry extends ListEntry<CreatureCount> {
    private final ReadOnlyStringWrapper level;

    public CreatureCountEntry(CreatureCount item) {
        super(item, item.getCreature().getName());
        level = new ReadOnlyStringWrapper(String.valueOf(item.getCreature().getLevel()));
    }

    public CreatureCountEntry(String label) {
        super(label);
        level = new ReadOnlyStringWrapper("");
    }

    @Override
    public ObservableValue<String> get(String propertyName) {
        switch (propertyName) {
            case "name":
                return nameProperty();
            case "level":
                return levelProperty();
            case "count":
                return getContents().countProperty().asString();
        }
        return null;
    }

    public ReadOnlyStringWrapper levelProperty() {
        return level;
    }
}
