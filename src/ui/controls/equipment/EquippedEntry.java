package ui.controls.equipment;

import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import model.enums.Slot;
import model.equipment.ItemCount;

public class EquippedEntry {
    private final ReadOnlyStringWrapper name, weight;
    private final ReadOnlyProperty<Integer> count;
    private final ReadOnlyObjectWrapper<Slot> slot;
    private final ItemCount itemCount;

    public EquippedEntry(ItemCount itemCount, Slot slot) {
        this.itemCount = itemCount;
        this.name = new ReadOnlyStringWrapper(itemCount.toString());
        itemCount.countProperty().addListener(c->name.set(itemCount.toString()));
        this.weight = new ReadOnlyStringWrapper(itemCount.stats().getPrettyWeight());
        this.count = itemCount.countProperty();
        this.slot = new ReadOnlyObjectWrapper<>(slot);
    }

    public ReadOnlyStringProperty nameProperty() {
        return name.getReadOnlyProperty();
    }
    public ReadOnlyStringProperty weightProperty() {
        return weight.getReadOnlyProperty();
    }
    public ReadOnlyProperty<Integer> countProperty() {
        return count;
    }

    public ObservableValue<Slot> slotProperty() {
        return slot.getReadOnlyProperty();
    }

    public ItemCount getItemCount() {
        return itemCount;
    }

    public Slot getSlot() {
        return slotProperty().getValue();
    }

    public Integer getCount() {
        return countProperty().getValue();
    }
}
