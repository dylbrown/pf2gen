package model.player;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import model.enums.Slot;
import model.equipment.Equipment;

import java.util.HashMap;
import java.util.Map;

public class InventoryManager {
    private final ReadOnlyObjectWrapper<Double> money= new ReadOnlyObjectWrapper<>(150.0);
    private final ObservableMap<Equipment, Equipment> inventory = FXCollections.observableHashMap();
    private final Map<Slot, Equipment> equipped = new HashMap<>();

    public Equipment getEquipped(Slot slot) {
        return equipped.get(slot);
    }

    public ReadOnlyObjectProperty<Double> getMoneyProperty() {
        return money.getReadOnlyProperty();
    }

    public boolean buy(Equipment item, int count) {
        Equipment newItem = item.copy();
        newItem.setCount(count);
        if(newItem.getValue() * count > money.get()) return false;
        money.set(money.get() - newItem.getValue() * count);
        inventory.put(item, newItem);
        return true;
    }

    public boolean sell(Equipment item, int count) {
        Equipment equipment = inventory.get(item);
        if(equipment == null) return false;
        int remaining = equipment.getCount();
        if(remaining <= 0) return false;
        money.set(money.get() + item.getValue() * count);
        equipment.remove(count);
        unequip(item, count);
        remaining -= count;
        if(remaining <= 0) {
            inventory.remove(item);
        }
        return true;
    }

    public double getTotalValue() {
        return inventory.values().stream().mapToDouble(equipment -> equipment.getValue() * equipment.getCount()).sum();
    }

    public int getCount(Equipment item) {
        Equipment equipment = inventory.get(item);
        if(equipment == null) return 0;
        return equipment.getCount();
    }

    public void addInventoryListener(MapChangeListener<Equipment, Equipment> listener) {
        inventory.addListener(listener);
    }

    public boolean equip(Equipment item, int count) {
        Slot slot = item.getSlot();
        Equipment slotContents = equipped.get(slot);
        if(slotContents == null) {
            equipped.put(slot, item);
            return true;
        }else if(slotContents.equals(item)) {
            slotContents.add(count);
            return true;
        }else return false;
    }

    public boolean unequip(Equipment item, int count) {
        Slot slot = item.getSlot();
        Equipment slotContents = equipped.get(slot);
        if(slotContents != null && slotContents.equals(item) && slotContents.getCount() >= count) {
            slotContents.remove(count);
            if(slotContents.getCount() == 0) {
                equipped.remove(slot);
            }
            return true;
        }
        return false;
    }

    public ObservableMap<Equipment, Equipment> getItems() {
        return FXCollections.unmodifiableObservableMap(inventory);
    }

    public void reset() {
        for (Equipment item : inventory.values()) {
            sell(item, item.getCount());
        }

    }
}
