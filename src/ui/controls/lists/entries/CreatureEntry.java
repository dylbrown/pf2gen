package ui.controls.lists.entries;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import model.creatures.Creature;

public class CreatureEntry extends ListEntry<Creature> {
    private final ReadOnlyStringWrapper level;

    public CreatureEntry(Creature creature) {
        super(creature, creature.getName());
        level = new ReadOnlyStringWrapper(String.valueOf(creature.getLevel()));
    }

    public CreatureEntry(String label) {
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
        }
        return null;
    }

    public ReadOnlyStringWrapper levelProperty() {
        return level;
    }
}
