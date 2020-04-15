package model.player;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import model.enums.BuySellMode;
import model.enums.Slot;
import model.equipment.Equipment;
import model.equipment.ItemCount;
import model.equipment.runes.Rune;
import model.equipment.runes.runedItems.Enchantable;
import model.equipment.runes.runedItems.RunedEquipment;
import model.util.Eyeball;
import model.util.Watcher;

public class InventoryManager {
    static final Double INITIAL_AMOUNT = 150.0;
    private final ReadOnlyObjectWrapper<Double> money= new ReadOnlyObjectWrapper<>(INITIAL_AMOUNT);
    private final ObservableMap<Equipment, ItemCount> inventory = FXCollections.observableHashMap();
    private final Eyeball<ItemCount, InventoryManager> buySellEye = new Eyeball<>(this);
    private final ObservableMap<Slot, ItemCount> equipped = FXCollections.observableHashMap();
    private final ObservableMap<Equipment, ItemCount> unequipped = FXCollections.observableHashMap();
    private double totalWeight = 0;
    private double sellMultiplier = 1;
    private double buyMultiplier = 1;

    public ItemCount getEquipped(Slot slot) {
        return equipped.get(slot);
    }

    public ReadOnlyObjectProperty<Double> getMoneyProperty() {
        return money.getReadOnlyProperty();
    }

    public boolean buy(Equipment item, int count) {
        if(count == 0) return false;
        if(item.getValue() * count * buyMultiplier > money.get()) return false;
        money.set(money.get() - item.getValue() * count * buyMultiplier);
        if(!add(item, count)) return false;
        ItemCount ic = inventory.computeIfAbsent(item, (key) -> new ItemCount(item, 0));
        return true;
    }

    private boolean add(Equipment item, int count) {
        if(count == 0) return false;
        //Add To Inventory
        ItemCount ic = inventory.computeIfAbsent(item, (key) -> new ItemCount(item, 0));
        ic.add(count);

        //Add To Unequipped
        unequipped.computeIfAbsent(item, (key)->new ItemCount(item, 0)).add(count);

        //Add To Total Weight
        totalWeight += item.getWeight() * count;
        buySellEye.wink(new ItemCount(ic, ic.getCount()-count), ic);
        return true;
    }

    public boolean sell(Equipment item, int count) {
        ItemCount ic = inventory.get(item);
        if(ic == null) return false;
        int remaining = ic.getCount();
        if(remaining - count < 0) return false;
        money.set(money.get() + item.getValue() * count * sellMultiplier);
        return remove(item, count);
    }

    private boolean remove(Equipment item, int count) {
        ItemCount ic = inventory.get(item);
        if(ic == null) return false;
        int remaining = ic.getCount();
        if(remaining - count < 0) return false;
        ic.remove(count);

        //Unequip Some if there aren't enough unequipped
        if(unequipped.get(item).getCount() < count)
            unequip(item, count - unequipped.get(item).getCount());

        //Remove from Unequipped
        unequipped.get(item).remove(count);
        if(unequipped.get(item).getCount() <= 0) unequipped.remove(item);

        remaining -= count;
        if(remaining <= 0) {
            inventory.remove(item);
        }

        //Remove From Total Weight
        totalWeight -= item.getWeight() * count;
        buySellEye.wink(new ItemCount(ic, ic.getCount()+count), ic);
        return true;
    }

    public double getTotalValue() {
        return inventory.values().stream().mapToDouble(item -> item.stats().getValue() * item.getCount()).sum();
    }

    public void addInventoryListener(MapChangeListener<Equipment, ItemCount> listener) {
        inventory.addListener(listener);
    }

    public void addBuySellWatcher(Watcher<ItemCount, InventoryManager> watcher) {
        buySellEye.addWatcher(watcher);
    }

    public boolean equip(Equipment item, Slot slot, int count) {
        //If trying to equip to OneHand, check it
        if(slot == Slot.OneHand){
            return equip(item, Slot.PrimaryHand, count) || equip(item, Slot.OffHand, count);
        }
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

        //Remove From Unequipped
        unequipped.get(item).remove(count);
        if(unequipped.get(item).getCount() <= 0) {
            unequipped.remove(item);
        }
        return true;
    }

    private boolean unequip(Equipment item, int count) {
        Slot slot = item.getSlot();

        if(slot == Slot.OneHand){
            return unequip(item, Slot.OffHand, count) || unequip(item, Slot.PrimaryHand, count);
        }else{
            return unequip(item, slot, count);
        }
    }

    public boolean unequip(Equipment item, Slot slot, int count) {
        ItemCount slotContents = equipped.get(slot);
        if(slotContents != null && slotContents.stats().equals(item) && slotContents.getCount() >= count) {
            slotContents.remove(count);
            if(slotContents.getCount() <= 0) {
                equipped.remove(slot);
            }

            //Add To Unequipped
            unequipped.computeIfAbsent(item, (key)->new ItemCount(item, 0)).add(count);
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

    public ObservableMap<Equipment, ItemCount> getUnequipped() {
        return FXCollections.unmodifiableObservableMap(unequipped);
    }

    public void reset() {
        money.set(INITIAL_AMOUNT);
        equipped.clear();
        unequipped.clear();
        inventory.clear();
        totalWeight = 0;
    }

    public double getTotalWeight() {
        return totalWeight;
    }

    public double getMoney() {
        return money.get();
    }

    public void addEquippedListener(MapChangeListener<Slot, ItemCount> listener) {
        equipped.addListener(listener);
    }

    public void setMoney(double amount) {
        money.set(amount);
    }

    public void setMode(BuySellMode mode) {
        switch (mode) {
            case FullPrice:
                buyMultiplier = sellMultiplier = 1;
                break;
            case SellHalf:
                buyMultiplier = 1;
                sellMultiplier = .5;
                break;
            case Cashless:
                buyMultiplier = sellMultiplier = 0;
                break;
        }
    }

    public void addMoney(double amount) {
        money.set(money.get() + amount);
    }

    private Equipment convertToRuned(Equipment item) {
        if(!getItems().containsKey(item)) return null;
        remove(item, 1);
        if(!(item instanceof Enchantable)) return null;
        Equipment runedItem = ((Enchantable) item).makeRuned();
        add(runedItem, 1);
        return runedItem;
    }

    private Equipment revertFromRuned(Equipment runedItem) {
        if(!getItems().containsKey(runedItem) || !(runedItem instanceof RunedEquipment)) return null;
        remove(runedItem, 1);
        Equipment item = ((RunedEquipment) runedItem).getBaseItem();
        add(item, 1);
        return item;
    }

    public Equipment tryToAddRune(Equipment item, Rune rune) {
        if(item instanceof RunedEquipment) {
            if(((RunedEquipment) item).getRunes().tryToAddRune(rune)){
                remove(rune, 1);
            }
            return item;
        }
        if(!rune.isFundamental()) return item;
        Equipment runedItem = convertToRuned(item);
        if(tryToAddRune(runedItem, rune) == null) {
            return revertFromRuned(runedItem);
        } else return runedItem;
    }

    public Equipment tryToRemoveRune(Equipment runedItem, Rune rune) {
        // Can we afford the rune
        if(rune.getValue() * .1 * buyMultiplier > money.get()) return null;

        if(runedItem instanceof RunedEquipment) {
            if(((RunedEquipment) runedItem).getRunes().tryToRemoveRune(rune)){
                add(rune, 1);
                money.set(money.get() - rune.getValue() * .1 * buyMultiplier);
            }
            if(((RunedEquipment) runedItem).getRunes().getAll().size() == 0) {
                return revertFromRuned(runedItem);
            } else return runedItem;
        } else return null;
    }
}
