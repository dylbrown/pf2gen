package model.equipment;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyProperty;

public class ItemCount {

    private final Equipment baseItem;
    private final ReadOnlyObjectWrapper<Integer> count;

    public ItemCount(Equipment baseItem, int count) {
        this.baseItem = baseItem;
        this.count = new ReadOnlyObjectWrapper<>(count);
    }

    public ItemCount(ItemCount source) {
        this.baseItem = source.baseItem;
        this.count = new ReadOnlyObjectWrapper<>(source.getCount());
    }

    public Equipment stats() {
        return baseItem;
    }

    public int getCount() {
        return count.get();
    }

    public ReadOnlyProperty<Integer> countProperty() {
        return count.getReadOnlyProperty();
    }

    public void add(int e) {
        count.set(count.get()+e);
    }

    public void remove(int e) {
        count.set(count.get()-e);
    }

    public void setCount(int count) {
        this.count.set(count);
    }

    @Override
    public String toString() {
        return count.get() + " " + baseItem.toString();
    }
}
