package model.player;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import model.enums.Slot;
import model.equipment.Equipment;
import model.equipment.ItemCount;

import java.util.Comparator;
import java.util.TreeMap;

public class InventoryManager {
    private final ReadOnlyObjectWrapper<Double> money= new ReadOnlyObjectWrapper<>(150.0);
    private final ObservableMap<Equipment, ItemCount> inventory = FXCollections.observableHashMap();
    private final ObservableMap<Slot, ItemCount> equipped = FXCollections.observableHashMap();
    private final ObservableMap<Equipment, ItemCount> carried = FXCollections.observableMap(new TreeMap<>(Comparator.comparing(Equipment::getName)));

    public ItemCount getEquipped(Slot slot) {
        return equipped.get(slot);
    }

    public ReadOnlyObjectProperty<Double> getMoneyProperty() {
        return money.getReadOnlyProperty();
    }

    public boolean buy(Equipment item, int count) {
        if(item.getValue() * count > money.get()) return false;
        money.set(money.get() - item.getValue() * count);

        if(inventory.get(item) != null) {
            inventory.get(item).add(count);
        }else{
            inventory.put(item, new ItemCount(item, count));
        }
        return true;
    }

    public boolean sell(Equipment equipment, int count) {
        ItemCount item = inventory.get(equipment);
        if(item == null) return false;
        int remaining = item.getCount();
        if(remaining <= 0) return false;
        money.set(money.get() + equipment.getValue() * count);
        item.remove(count);
        unequip(equipment, count);
        remaining -= count;
        if(remaining <= 0) {
            inventory.remove(equipment);
        }
        return true;
    }

    public double getTotalValue() {
        return inventory.values().stream().mapToDouble(item -> item.stats().getValue() * item.getCount()).sum();
    }

    public void addInventoryListener(MapChangeListener<Equipment, ItemCount> listener) {
        inventory.addListener(listener);
    }

    public boolean equip(Equipment item, Slot slot, int count) {
        //If only carried, don't put in slot
        if(slot != Slot.Carried) {
            //Handle Hand Shenanigans
            if(item.getSlot() != slot)
                if(item.getSlot() != Slot.OneHand || (slot != Slot.PrimaryHand && slot != Slot.OffHand))
                    return false;
            //Handle the Two Handed Weapons
            if(slot == Slot.TwoHands && (equipped.get(Slot.PrimaryHand) != null || equipped.get(Slot.OffHand) != null)) return false;
            if((slot == Slot.PrimaryHand || slot == Slot.OffHand) && equipped.get(Slot.TwoHands) != null) return false;

            ItemCount slotContents = equipped.get(slot);
            if(slotContents == null) {
                equipped.put(slot, new ItemCount(item, count));
            }else if(slotContents.stats().equals(item)) {
                slotContents.add(count);
            }else return false;
        }

        //Add To Carried
        if(carried.get(item) == null){
            carried.put(item, new ItemCount(item, count));
        }else {
            carried.get(item).add(count);
        }
        return true;
    }

    private boolean unequip(Equipment item, int count) {
        Slot slot = item.getSlot();
        ItemCount slotContents = equipped.get(slot);
        if(slotContents != null && slotContents.stats().equals(item) && slotContents.getCount() >= count) {
            slotContents.remove(count);
            carried.get(item).remove(count);
            if(slotContents.getCount() == 0) {
                equipped.remove(slot);
                carried.remove(item);
            }
            return true;
        }

        if(slot == Slot.OneHand){
            if(unequip(item, Slot.PrimaryHand, count)) return true;
            if(unequip(item, Slot.OffHand, count)) return true;
        }


        ItemCount carriedCount = carried.get(item);
        if(carriedCount != null && carriedCount.getCount() >= count) {
            carriedCount.remove(count);
            if(carriedCount.getCount() == 0) carried.remove(item);
            return true;
        }
        return false;
    }

    public boolean unequip(Equipment item, Slot slot, int count) {
        ItemCount slotContents = equipped.get(slot);
        if(slotContents != null && slotContents.stats().equals(item) && slotContents.getCount() >= count) {
            slotContents.remove(count);
            carried.get(item).remove(count);
            if(slotContents.getCount() == 0)
                equipped.remove(slot);
            if(carried.get(item).getCount() == 0)
                carried.remove(item);
            return true;
        }
        return false;
    }

    public ObservableMap<Equipment, ItemCount> getItems() {
        return FXCollections.unmodifiableObservableMap(inventory);
    }

    public ObservableMap<Slot, ItemCount> getEquipped() {
        return FXCollections.unmodifiableObservableMap(equipped);
    }

    public ObservableMap<Equipment, ItemCount> getCarried() {
        return FXCollections.unmodifiableObservableMap(carried);
    }

    public void reset() {
        for (ItemCount item : inventory.values()) {
            sell(item.stats(), item.getCount());
        }

    }

    public void addEquippedListener(MapChangeListener<Slot, ItemCount> listener) {
        equipped.addListener(listener);
    }
}
