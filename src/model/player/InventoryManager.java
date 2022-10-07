package model.player;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import model.abilities.Ability;
import model.abilities.FormulaExtension;
import model.enums.BuySellMode;
import model.enums.Slot;
import model.items.*;
import model.items.armor.Armor;
import model.items.armor.Shield;
import model.items.runes.Rune;
import model.items.runes.runedItems.Enchantable;
import model.items.runes.runedItems.RunedArmor;
import model.items.runes.runedItems.RunedWeapon;
import model.items.runes.runedItems.Runes;
import model.util.Eyeball;
import model.util.Pair;
import model.util.Watcher;

import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

public class InventoryManager implements PlayerState {
    static final Double INITIAL_AMOUNT = 150.0;
    private final ReadOnlyObjectWrapper<Double> money= new ReadOnlyObjectWrapper<>(INITIAL_AMOUNT);
    private final ObservableMap<Item, ItemCount> inventory = FXCollections.observableHashMap();
    private final Eyeball<ItemCount, InventoryManager> buySellEye = new Eyeball<>(this);
    private final ObservableMap<Slot, ItemCount> equipped = FXCollections.observableHashMap();
    private final ObservableMap<Item, ItemCount> carried = FXCollections.observableHashMap();
    private final ObservableMap<Item, ItemCount> unequipped = FXCollections.observableHashMap();
    private final ObservableSet<Item> formulasBought = FXCollections.observableSet(new HashSet<>());
    private final ObservableSet<Item> formulasGranted = FXCollections.observableSet(new HashSet<>());
    private final ObservableMap<Integer, Integer> grantedFormulasCount = FXCollections.observableMap(new TreeMap<>());
    private final Applier<Item> itemApplier;
    private double totalWeight = 0;
    private double sellMultiplier = 1;
    private double buyMultiplier = 1;

    InventoryManager(Applier<Ability> applier, Applier<Item> itemApplier) {
        this.itemApplier = itemApplier;
        applier.onApply(a->{
            FormulaExtension formulas = a.getExtension(FormulaExtension.class);
            if(formulas != null) {
                for (Map.Entry<Integer, Integer> entry : formulas.getFormulasKnown().entrySet()) {
                    grantedFormulasCount.merge(entry.getKey(), entry.getValue(), Integer::sum);
                }
            }
        });
        applier.onRemove(a->{
            FormulaExtension formulas = a.getExtension(FormulaExtension.class);
            if(formulas != null) {
                for (Map.Entry<Integer, Integer> entry : formulas.getFormulasKnown().entrySet()) {
                    grantedFormulasCount.merge(entry.getKey(), -entry.getValue(), Integer::sum);
                }
            }
        });
    }

    public ItemCount getEquipped(Slot slot) {
        return equipped.get(slot);
    }

    public ReadOnlyObjectProperty<Double> getMoneyProperty() {
        return money.getReadOnlyProperty();
    }

    public boolean buy(Item item, int count) {
        if(count == 0) return false;
        if(item.getValue() * count * buyMultiplier > money.get()) return false;
        money.set(money.get() - item.getValue() * count * buyMultiplier);
        if(!add(item, count)) return false;
        inventory.computeIfAbsent(item, (key) -> new ItemCount(item, 0));
        return true;
    }

    private boolean add(Item item, int count) {
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

    public boolean sell(Item item, int count) {
        ItemCount ic = inventory.get(item);
        if(ic == null) return false;
        int remaining = ic.getCount();
        if(remaining - count < 0) return false;
        money.set(money.get() + item.getValue() * count * sellMultiplier);
        return remove(item, count);
    }

    private boolean remove(Item item, int count) {
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

    public void addInventoryListener(MapChangeListener<Item, ItemCount> listener) {
        inventory.addListener(listener);
    }

    public void addBuySellWatcher(Watcher<ItemCount, InventoryManager> watcher) {
        buySellEye.addWatcher(watcher);
    }

    public boolean equip(Item item, Slot slot, int count) {
        if(unequipped.get(item) == null) return false;
        if(count == 0) return false;
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
        itemApplier.preApply(item);
        itemApplier.apply(item);
        return true;
    }

    private boolean unequip(Item item, int count) {
        Slot slot = item.getSlot();

        if(slot == Slot.OneHand){
            return unequip(item, Slot.OffHand, count) || unequip(item, Slot.PrimaryHand, count);
        }else{
            return unequip(item, slot, count);
        }
    }

    public boolean unequip(Item item, Slot slot, int count) {
        if(count == 0) return false;
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
        itemApplier.remove(item);
        return true;
    }

    public ObservableMap<Item, ItemCount> getItems() {
        return FXCollections.unmodifiableObservableMap(inventory);
    }

    public ObservableMap<Slot, ItemCount> getEquipped() {
        return FXCollections.unmodifiableObservableMap(equipped);
    }

    public ObservableMap<Item, ItemCount> getUnequipped() {
        return FXCollections.unmodifiableObservableMap(unequipped);
    }

    private final ObservableMap<Item, ItemCount> carriedUnmod =
            FXCollections.unmodifiableObservableMap(carried);
    public ObservableMap<Item, ItemCount> getCarried() {
        return carriedUnmod;
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

    private Item convertToRuned(Item item) {
        if(!getItems().containsKey(item)) return null;
        Enchantable enchantable = item.getExtension(Enchantable.class);
        if(enchantable == null || !enchantable.isEnchantable()) return null;
        boolean equip = (getEquipped(item.getSlot()) != null) && item.equals(getEquipped(item.getSlot()).stats());
        remove(item, 1);
        ItemInstance runedItem;
        if(item instanceof BaseItem)
            runedItem = new ItemInstance(item);
        else if(item instanceof ItemInstance)
            runedItem = (ItemInstance) item;
        else return null;
        if(item.hasExtension(Armor.class) || item.hasExtension(Shield.class))
            runedItem.addExtension(RunedArmor.class);
        else
            runedItem.addExtension(RunedWeapon.class);
        add(runedItem, 1);
        if(equip) equip(runedItem, runedItem.getSlot(), 1);
        return runedItem;
    }

    private Item revertFromRuned(Item runedItem) {
        if(!getItems().containsKey(runedItem) ||
                (!runedItem.hasExtension(RunedArmor.class) &&
                        runedItem.hasExtension(RunedWeapon.class))
                || !(runedItem instanceof ItemInstance))
            return null;
        boolean equip = (getEquipped(runedItem.getSlot()) != null) && runedItem.equals(getEquipped(runedItem.getSlot()).stats());
        remove(runedItem, 1);
        Item item = ((ItemInstance) runedItem).getSourceItem();
        add(item, 1);
        if(equip) equip(item, item.getSlot(), 1);
        return item;
    }

    public Pair<Boolean, Item> tryToAddRune(Item item, Rune rune) {
        if(item instanceof ItemInstance) {
            Runes<?> runes = Runes.getRunes(item);
            if(runes != null) {
                if (runes.tryToAddRune(rune)) {
                    remove(rune.getItem(), 1);
                    return new Pair<>(true, item);
                } else return new Pair<>(false, item);
            }
        }
        if(!rune.isFundamental()) return new Pair<>(false, item);
        Item runedItem = convertToRuned(item);
        if(runedItem == null) return new Pair<>(false, item);
        Pair<Boolean, Item> result = tryToAddRune(runedItem, rune);
        if(result.first)
            return result;
        else return new Pair<>(false, revertFromRuned(runedItem));
    }

    public Pair<Boolean, Item> tryToRemoveRune(Item runedItem, Rune rune) {
        // Can we afford the rune
        if(rune.getItem().getValue() * .1 * buyMultiplier > money.get()) return new Pair<>(false, runedItem);

        Runes<?> runes = Runes.getRunes(runedItem);
        if(runes != null) {
            if(runes.tryToRemoveRune(rune)){
                add(rune.getItem(), 1);
                money.set(money.get() - rune.getItem().getValue() * .1 * buyMultiplier);
            }
            if(runes.getAll().size() == 0) {
                return new Pair<>(true, revertFromRuned(runedItem));
            } else return new Pair<>(true, runedItem);
        } else return new Pair<>(false, runedItem);
    }

    public boolean tryToUpgradeRune(Item runedItem, Rune rune, Rune upgradedRune) {
        if((upgradedRune.getItem().getValue() - rune.getItem().getValue()) * buyMultiplier > money.get()) return false;

        Runes<?> runes = Runes.getRunes(runedItem);
        if(runes != null) {
            if(runes.tryToUpgradeRune(rune, upgradedRune)){
                money.set(money.get() - (upgradedRune.getItem().getValue() - rune.getItem().getValue()) * buyMultiplier);
                return true;
            }
        }
        return false;
    }

    private final ObservableSet<Item> unmodifiableBought = FXCollections.unmodifiableObservableSet(formulasBought);
    public ObservableSet<Item> getFormulasBought() {
        return unmodifiableBought;
    }

    private final ObservableSet<Item> unmodifiableGranted = FXCollections.unmodifiableObservableSet(formulasGranted);
    public ObservableSet<Item> getFormulasGranted() {
        return unmodifiableGranted;
    }

    public boolean addFormula(Item formula, boolean buy) {
        if(!(formula instanceof ItemFormula))
            formula = new ItemFormula(formula);
        if(formulasBought.contains(formula) || formulasGranted.contains(formula))
            return false;
        if(buy) {
            if (money.get() >= formula.getValue() * buyMultiplier) {
                money.set(money.get() - (formula.getValue() * buyMultiplier));
                formulasBought.add(formula);
                return true;
            }
        } else {
            formulasGranted.add(formula);
            return true;
        }
        return false;
    }

    public void removeFormula(Item formula) {
        if(!(formula instanceof ItemFormula))
            formula = new ItemFormula(formula);
        if(formulasBought.contains(formula)) {
            money.set(money.get() + (formula.getValue() * buyMultiplier));
            formulasBought.remove(formula);
            return;
        }
        formulasGranted.remove(formula);
    }
    private final ObservableMap<Integer, Integer> knownHolder = FXCollections.unmodifiableObservableMap(grantedFormulasCount);
    public ObservableMap<Integer, Integer> getGrantedFormulasCount() {
        return knownHolder;
    }

    @Override
    public void reset(PC.ResetEvent resetEvent) {
        if(!resetEvent.isActive()) return;
        money.set(INITIAL_AMOUNT);
        equipped.clear();
        unequipped.clear();
        inventory.clear();
        totalWeight = 0;
    }
}
