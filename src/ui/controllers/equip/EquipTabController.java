package ui.controllers.equip;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import model.CharacterManager;
import model.enums.Slot;
import model.items.Item;
import model.items.ItemCount;
import model.player.PC;
import ui.controls.equipment.EquippedEntry;

import java.util.*;
import java.util.function.Function;

public class EquipTabController {
    @FXML
    private TabPane tabs;
    @FXML
    private Tab tab_purchase, tab_enchant, tab_equip, tab_formulas;
    @FXML
    private AnchorPane purchaseTab;
    @FXML
    PurchaseTabController purchaseTabController;
    @FXML
    private TableColumn<EquippedEntry, String> nameCol,weightCol;
    @FXML
    private TableColumn<EquippedEntry, Slot> slotCol;
    @FXML
    private TableColumn<EquippedEntry, Integer> quantityCol;
    @FXML
    private ListView<ItemCount> unequipped;
    @FXML
    private TableView<EquippedEntry> equipped;

    //Inventory List
    private final ObservableList<ItemCount> inventoryList = FXCollections.observableArrayList(count -> new Observable[]{count.countProperty()});

    //Unequipped List
    private final ObservableList<ItemCount> unequipList = FXCollections.observableArrayList(count -> new Observable[]{count.countProperty()});
    private final FilteredList<ItemCount> unequipFilter = new FilteredList<>(unequipList);
    private final SortedList<ItemCount> unequipSort = new SortedList<>(unequipFilter, Comparator.comparing(o -> o.stats().getName()));
    private final Map<Item, ItemCount> unequipMap = new HashMap<>();

    //Equipped List
    private final ObservableList<EquippedEntry> equipList = FXCollections.observableArrayList(entry -> new Observable[]{entry.countProperty()});
    private final FilteredList<EquippedEntry> equipFilter = new FilteredList<>(equipList);
    private PC character;

    @FXML
    private void initialize() {
        purchaseTabController.passValues(inventoryList);
        character = CharacterManager.getActive();
        // itemDisplay.setZoom(1.25);
        nameCol.setCellValueFactory(param -> param.getValue().nameProperty());
        slotCol.setCellValueFactory(param -> param.getValue().slotProperty());
        weightCol.setCellValueFactory(param -> param.getValue().weightProperty());
        quantityCol.setCellValueFactory(param -> param.getValue().countProperty());
        equipped.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        for (TableColumn<EquippedEntry, ?> column : equipped.getColumns()) {
            column.setReorderable(false);
        }
        unequipped.setOnMouseClicked((event) -> {
            if(event.getClickCount() % 2 == 0) {
                ItemCount item = unequipped.getSelectionModel().getSelectedItem();
                tryToEquip(item);
            }
        });

        equipped.setOnMouseClicked(event -> {
            if(event.getClickCount() % 2 == 0) {
                EquippedEntry item = equipped.getSelectionModel().getSelectedItem();
                if(item == null) return;
                tryToUnequip(item.getItemCount(), item.slotProperty().getValue());
            }
        });

        unequipList.addListener((ListChangeListener<ItemCount>) c -> {
            while(c.next()) {
                for (ItemCount itemCount : c.getAddedSubList()) {
                    unequipMap.put(itemCount.stats(), itemCount);
                }
                for (ItemCount itemCount : c.getRemoved()) {
                    unequipMap.remove(itemCount.stats());
                }
            }
        });

        unequipFilter.setPredicate(item -> item.getCount() > 0);
        equipFilter.setPredicate(pair -> pair.countProperty().getValue() > 0);

        unequipped.setItems(unequipSort);
        equipped.setItems(equipFilter);

        character.inventory().addInventoryListener(change -> {
            if(change.wasRemoved()) {
                ItemCount valueRemoved = change.getValueRemoved();
                inventoryList.remove(valueRemoved);
                unequipList.removeIf(item->item.stats() == valueRemoved.stats());
                equipList.removeIf(pair -> pair.getItemCount().stats() == valueRemoved.stats());
            }
            if(change.wasAdded()) {
                ItemCount valueAdded = change.getValueAdded();
                addHandler(valueAdded);
            }
        });
        for (ItemCount itemCount : character.inventory().getItems().values()) {
            addHandler(itemCount);
        }
        for (Map.Entry<Slot, ItemCount> entry : character.inventory().getEquipped().entrySet()) {
            updateFromEquip(unequipMap.get(entry.getValue().stats()), entry.getKey());
        }


        character.inventory().addEquippedListener(change -> {
            if(change.wasAdded() && !controllerOp) {
                ItemCount unequippedItem = unequipMap.get(change.getValueAdded().stats());
                if(unequippedItem != null)
                    updateFromEquip(unequippedItem, change.getKey());
            }
        });
        character.inventory().getCarried().addListener(
        (MapChangeListener<Item, ItemCount>) change -> {
            if(change.wasAdded() && !controllerOp) {
                ItemCount unequippedItem = unequipMap.get(change.getValueAdded().stats());
                if(unequippedItem != null)
                    updateFromEquip(unequippedItem, Slot.Carried);
            }
        });
    }

    private void addHandler(ItemCount valueAdded) {
        inventoryList.add(valueAdded);
        ItemCount unequippedItem = new ItemCount(valueAdded.stats(), valueAdded.getCount());
        unequipList.add(unequippedItem);
        valueAdded.countProperty().addListener((o,oldVal,newVal)->{
            if (newVal > oldVal) {
                unequippedItem.add(newVal - oldVal);
            }else if (oldVal  > newVal){
                if(unequippedItem.getCount() >= oldVal - newVal)
                    unequippedItem.remove(oldVal - newVal);
                else {
                    ItemCount equipCount = find(equipFilter, valueAdded.stats(), EquippedEntry::getItemCount);
                    if(equipCount != null) {
                        equipCount.remove(oldVal - newVal);
                    }
                }
            }
        });
    }

    private boolean controllerOp = false;
    private void tryToEquip(ItemCount unequippedItem) {
        controllerOp = true;
        Slot slot = unequippedItem.stats().getSlot();
        if(slot == Slot.OneHand) {
            List<Slot> choices = new ArrayList<>(Arrays.asList(Slot.PrimaryHand, Slot.OffHand, Slot.Carried));

            ChoiceDialog<Slot> dialog = new ChoiceDialog<>(Slot.PrimaryHand, choices);
            dialog.setTitle("Slot Choice");
            dialog.setHeaderText("Choose a Slot");
            dialog.setContentText("");

            Optional<Slot> result = dialog.showAndWait();
            if (result.isEmpty()) {
                controllerOp = false;
                return;
            }
            slot = result.get();
        }
        if(slot == Slot.None) slot = Slot.Carried;
        if(character.inventory().equip(unequippedItem.stats(), slot, 1)) {
            updateFromEquip(unequippedItem, slot);
        }
        controllerOp = false;
    }

    private void updateFromEquip(ItemCount unequippedItem, Slot slot) {
        boolean alreadyEquipped = false;
        for (EquippedEntry entry : equipList) {
            if(entry.getItemCount().stats() == unequippedItem.stats()
                    && entry.getSlot() == slot
                    && entry.getCount() > 0) {
                alreadyEquipped = true;
                break;
            }
        }
        if(!alreadyEquipped) {
            EquippedEntry entry = new EquippedEntry(unequippedItem.copy(), slot);
            equipList.add(entry);
        } else {
            ItemCount equippedItem = find(equipFilter, unequippedItem.stats(), EquippedEntry::getItemCount);
            if (equippedItem != null) {
                equippedItem.add(1);
            }
        }
        unequippedItem.remove(unequippedItem.getCount());
    }

    private void tryToUnequip(ItemCount equippedItem, Slot slot) {
        if(character.inventory().unequip(equippedItem.stats(), slot,1)) {
            equippedItem.remove(1);
            ItemCount unequippedItem = unequipMap.get(equippedItem.stats());
            if(unequippedItem != null)
                unequippedItem.add(1);
            if(equippedItem.getCount() == 0)
                equipList.removeIf(entry->(entry.getItemCount() == equippedItem) && (entry.getSlot() == slot));
            unequipped.refresh();
            equipped.refresh();
        }
    }

    private <T> ItemCount find(List<T> list, Item stats, Function<T,ItemCount> converter) {
        int first = 0; int last = list.size();
        while(first < last) {
            ItemCount count = converter.apply(list.get((first + last) / 2));
            if(count.stats().compareTo(stats) == 0)
                return count;
            else if(count.stats().compareTo(stats) > 0)
                last = (first + last) / 2;
            else
                first = (first + last) / 2 + 1;
        }
        return null;
    }

    public void navigate(List<String> path) {
        switch (path.get(0)) {
            case "tab_purchase":
                tabs.getSelectionModel().select(tab_purchase);
                break;
            case "tab_equip":
                tabs.getSelectionModel().select(tab_equip);
                break;
            case "tab_enchant":
                tabs.getSelectionModel().select(tab_enchant);
                break;
            case "tab_formulas":
                tabs.getSelectionModel().select(tab_formulas);
                break;
        }
    }
}
