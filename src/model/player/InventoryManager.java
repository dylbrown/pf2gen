package model.player;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import model.attributes.AttributeBonus;
import model.enums.BuySellMode;
import model.enums.Slot;
import model.equipment.Equipment;
import model.equipment.ItemCount;
import model.equipment.UnmodifiableItemCount;
import model.equipment.runes.Rune;
import model.equipment.runes.runedItems.Enchantable;
import model.equipment.runes.runedItems.RunedArmor;
import model.equipment.runes.runedItems.RunedEquipment;
import model.util.Eyeball;
import model.util.Pair;
import model.util.Watcher;

@SuppressWarnings("rawtypes")
public class InventoryManager {
    static final Double INITIAL_AMOUNT = 150.0;
    private final ReadOnlyObjectWrapper<Double> money= new ReadOnlyObjectWrapper<>(INITIAL_AMOUNT);
    private final ObservableMap<Equipment, ItemCount> inventory = FXCollections.observableHashMap();
    private final Eyeball<ItemCount, InventoryManager> buySellEye = new Eyeball<>(this);
    private final ObservableMap<Slot, ItemCount> equipped = FXCollections.observableHashMap();
    private final ObservableMap<Equipment, ItemCount> carried = FXCollections.observableHashMap();
    private final ObservableMap<Equipment, ItemCount> unequipped = FXCollections.observableHashMap();
    private final AttributeManager attributes;
    private double totalWeight = 0;
    private double sellMultiplier = 1;
    private double buyMultiplier = 1;

    InventoryManager(AttributeManager attributes) {
        this.attributes = attributes;
    }

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
        if(unequipped.get(item) == null)
            unequip(item, count);
        else if(unequipped.get(item).getCount() < count)
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
        buySellEye.wink(new UnmodifiableItemCount(ic, ic.getCount()+count), new UnmodifiableItemCount(ic));
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
        if(unequipped.get(item) == null) return false;
        if(slot == Slot.None) return false;
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
        } else {
            carried.computeIfAbsent(item, k->new ItemCount(k, 0)).add(count);
        }

        //Remove From Unequipped
        unequipped.get(item).remove(count);
        if(unequipped.get(item).getCount() <= 0) {
            unequipped.remove(item);
        }
        attributes.apply(item.getBonuses());
        if(item instanceof RunedArmor) {
            ((RunedArmor) item).getBonuses().addListener((ListChangeListener<AttributeBonus>) c->{
                while(c.next()) {
                    if(c.wasAdded())
                        attributes.apply(c.getAddedSubList());
                    if(c.wasRemoved())
                        attributes.remove(c.getRemoved());
                }
            });
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
        if(slot != Slot.Carried) {
            ItemCount slotContents = equipped.get(slot);
            if (slotContents != null && slotContents.stats().equals(item) && slotContents.getCount() >= count) {
                slotContents.remove(count);
                if (slotContents.getCount() <= 0) {
                    equipped.remove(slot);
                }
            } else return false;
        } else {
            //Add To Unequipped
            carried.get(item).remove(count);
            if(carried.get(item).getCount() <= 0) {
                carried.remove(item);
            }
        }
        unequipped.computeIfAbsent(item, (key) -> new ItemCount(item, 0)).add(count);
        attributes.remove(item.getBonuses());
        return true;
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

    private final ObservableMap<Equipment, ItemCount> carriedUnmod =
            FXCollections.unmodifiableObservableMap(carried);
    public ObservableMap<Equipment, ItemCount> getCarried() {
        return carriedUnmod;
    }

    public void reset() {
        money.set(INITIAL_AMOUNT);
        equipped.clear();
        unequipped.clear();
        inventory.clear();
        totalWeight = 0;
    }

    @SuppressWarnings("WeakerAccess")
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
        if(!(item instanceof Enchantable)) return null;
        boolean equip = (getEquipped(item.getSlot()) != null) && item.equals(getEquipped(item.getSlot()).stats());
        remove(item, 1);
        Equipment runedItem = ((Enchantable) item).makeRuned();
        add(runedItem, 1);
        if(equip) equip(runedItem, runedItem.getSlot(), 1);
        return runedItem;
    }

    private Equipment revertFromRuned(Equipment runedItem) {
        if(!getItems().containsKey(runedItem) || !(runedItem instanceof RunedEquipment)) return null;
        boolean equip = (getEquipped(runedItem.getSlot()) != null) && runedItem.equals(getEquipped(runedItem.getSlot()).stats());
        remove(runedItem, 1);
        Equipment item = ((RunedEquipment) runedItem).getBaseItem();
        add(item, 1);
        if(equip) equip(item, item.getSlot(), 1);
        return item;
    }

    public Pair<Boolean, Equipment> tryToAddRune(Equipment item, Rune rune) {
        if(item instanceof RunedEquipment) {
            if(((RunedEquipment) item).getRunes().tryToAddRune(rune)){
                remove(rune, 1);
                return new Pair<>(true, item);
            }else return new Pair<>(false, item);
        }
        if(!rune.isFundamental()) return new Pair<>(false, item);
        Equipment runedItem = convertToRuned(item);
        if(runedItem == null) return new Pair<>(false, item);
        Pair<Boolean, Equipment> result = tryToAddRune(runedItem, rune);
        if(result.first)
            return result;
        else return new Pair<>(false, revertFromRuned(runedItem));
    }

    public Pair<Boolean, Equipment> tryToRemoveRune(Equipment runedItem, Rune rune) {
        // Can we afford the rune
        if(rune.getValue() * .1 * buyMultiplier > money.get()) return new Pair<>(false, runedItem);

        if(runedItem instanceof RunedEquipment) {
            if(((RunedEquipment) runedItem).getRunes().tryToRemoveRune(rune)){
                add(rune, 1);
                money.set(money.get() - rune.getValue() * .1 * buyMultiplier);
            }
            if(((RunedEquipment) runedItem).getRunes().getAll().size() == 0) {
                return new Pair<>(true, revertFromRuned(runedItem));
            } else return new Pair<>(true, runedItem);
        } else return new Pair<>(false, runedItem);
    }

    public boolean tryToUpgradeRune(Equipment runedItem, Rune rune, Rune upgradedRune) {
        if((upgradedRune.getValue() - rune.getValue()) * buyMultiplier > money.get()) return false;

        if(runedItem instanceof RunedEquipment) {
            if(((RunedEquipment) runedItem).getRunes().tryToUpgradeRune(rune, upgradedRune)){
                money.set(money.get() - (upgradedRune.getValue() - rune.getValue()) * buyMultiplier);
                return true;
            }
        }
        return false;
    }
}
