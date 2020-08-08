package ui.controls.lists.entries;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import model.creatures.Creature;

public class CreatureCount {
    private final Creature creature;
    private final ReadOnlyIntegerWrapper count;

    public CreatureCount(Creature creature, int count) {
        this.creature = creature;
        this.count = new ReadOnlyIntegerWrapper(count);
    }

    public Creature getCreature() {
        return creature;
    }

    public int getCount() {
        return count.getValue();
    }

    public ReadOnlyIntegerProperty countProperty() {
        return count.getReadOnlyProperty();
    }

    public void add(int i) {
        count.set(count.get() + i);
    }

    public void remove(int i) {
        count.set(count.get() - i);
    }
}
