package ui.controllers;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.web.WebView;
import javafx.util.StringConverter;
import model.enums.BuySellMode;
import model.enums.Slot;
import model.equipment.Equipment;
import model.equipment.ItemCount;
import model.util.OPair;
import ui.Main;
import ui.controls.equipment.EquipmentHTMLGenerator;
import ui.controls.equipment.EquipmentList;
import ui.controls.equipment.ItemEntry;

import java.util.*;
import java.util.function.Function;

public class EquipTabController {

    @FXML
    private TableColumn<OPair<ItemCount, Slot>, String> nameCol;
    @FXML
    private TableColumn<OPair<ItemCount, Slot>, Slot> slotCol;
    @FXML
    private TableColumn<OPair<ItemCount, Slot>, String> weightCol;
    @FXML
    private TableColumn<OPair<ItemCount, Slot>, Integer> quantityCol;
    @FXML
    private TreeTableView<ItemEntry> allItems;
    @FXML
    private ListView<ItemCount> inventory, unequipped;
    @FXML
    private TableView<OPair<ItemCount, Slot>> equipped;
    @FXML
    public GridPane itemGrid;
    @FXML
    private Label money, totalValue;
    @FXML
    private ComboBox<BuySellMode> multiplier;
    @FXML
    private Button addMoney;

    @FXML
    private WebView itemDisplay;
    @FXML
    private RadioMenuItem nameSort, priceSort, ascSort, descSort;
    private final ToggleGroup sortBy = new ToggleGroup(); private final ToggleGroup direction = new ToggleGroup();
    private double value = 0;

    //Inventory List
    private final ObservableList<ItemCount> inventoryList = FXCollections.observableArrayList(count -> new Observable[]{count.countProperty()});

    //Unequipped List
    private final ObservableList<ItemCount> unequipList = FXCollections.observableArrayList(count -> new Observable[]{count.countProperty()});
    private final FilteredList<ItemCount> unequipFilter = new FilteredList<>(unequipList);
    private final SortedList<ItemCount> unequipSort = new SortedList<>(unequipFilter, Comparator.comparing(o -> o.stats().getName()));
    private final Map<Equipment, ItemCount> unequipMap = new HashMap<>();

    //Equipped List
    private final ObservableList<OPair<ItemCount, Slot>> equipList = FXCollections.observableArrayList(pair -> new Observable[]{pair.first.get().countProperty()});
    private final FilteredList<OPair<ItemCount, Slot>> equipFilter = new FilteredList<>(equipList);
    private final SortedList<OPair<ItemCount, Slot>> equipSort = new SortedList<>(equipFilter, (o1, o2) -> {
        int i = o1.second.get().compareTo(o1.second.get());
        if(i == 0) i = o1.first.get().stats().compareTo(o2.first.get().stats());
        return i;
    });

    @FXML
    private void initialize() {
        itemDisplay.setZoom(1.25);
        EquipmentList.init(allItems, this::tryToBuy);
        nameCol.setCellValueFactory(param -> param.getValue().first.get().stats().nameProperty());
        slotCol.setCellValueFactory(param -> param.getValue().second);
        weightCol.setCellValueFactory(param -> param.getValue().first.get().stats().nameProperty());
        quantityCol.setCellValueFactory(param -> param.getValue().first.get().countProperty());


        money.setText(Main.character.inventory().getMoneyProperty().get()+" sp");
        Main.character.inventory().getMoneyProperty().addListener((event)->
                money.setText(Main.character.inventory().getMoneyProperty().get()+" sp"));
        value = Main.character.inventory().getTotalValue();
        totalValue.setText(value+" sp");

        //Set up sort options



        allItems.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selectedItem) -> {
            Equipment item = selectedItem.getValue().getItem();
            if(item != null) setDisplay(item);
        });
        inventory.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selectedItem) -> {
            if(selectedItem != null) setDisplay(selectedItem.stats());
        });
        inventory.setOnMouseClicked((event) -> {
            if(event.getClickCount() % 2 == 0) {
                ItemCount item = inventory.getSelectionModel().getSelectedItem();
                tryToSell(item);
            }
        });
        unequipped.setOnMouseClicked((event) -> {
            if(event.getClickCount() % 2 == 0) {
                ItemCount item = unequipped.getSelectionModel().getSelectedItem();
                tryToEquip(item);
            }
        });

        equipped.setOnMouseClicked(event -> {
            if(event.getClickCount() % 2 == 0) {
                OPair<ItemCount, Slot> item = equipped.getSelectionModel().getSelectedItem();
                tryToUnequip(item.first.get(), item.second.get());
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
        equipFilter.setPredicate(pair -> pair.first.get().getCount() > 0);

        inventory.setItems(new SortedList<>(inventoryList, Comparator.comparing(o -> o.stats().toString())));
        unequipped.setItems(unequipSort);
        equipped.setItems(equipSort);

        Main.character.inventory().addInventoryListener(change -> {
            if(change.wasRemoved()) {
                ItemCount valueRemoved = change.getValueRemoved();
                inventoryList.remove(valueRemoved);
                unequipList.removeIf(item->item.stats() == valueRemoved.stats());
                equipList.removeIf(pair -> pair.first.get() == valueRemoved);
            }
            if(change.wasAdded()) {
                ItemCount valueAdded = change.getValueAdded();
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
                            ItemCount equipCount = find(equipSort, valueAdded.stats(), opair -> opair.first.get());
                            if(equipCount != null) {
                                equipCount.remove(oldVal - newVal);
                            }
                        }
                    }
                });
            }
            refreshTotalValue();
        });

        Main.character.inventory().addEquippedListener(change -> {
            if(change.wasAdded() && !controllerOp) {
                ItemCount unequippedItem = unequipMap.get(change.getValueAdded().stats());
                if(unequippedItem != null)
                    updateFromEquip(unequippedItem, change.getKey());
            }
        });

        multiplier.getItems().addAll(BuySellMode.values());
        multiplier.setConverter(new StringConverter<>() {
            @Override
            public String toString(BuySellMode object) {
                switch (object) {
                    case Normal:
                        return "Normal";
                    case SellHalf:
                        return "Sell Half";
                    case Cashless:
                        return "Cashless";
                }
                return null;
            }

            @Override
            public BuySellMode fromString(String string) {
                switch (string) {
                    case "Normal":
                        return BuySellMode.Normal;
                    case "Sell Half":
                        return BuySellMode.SellHalf;
                    case "Cashless":
                        return BuySellMode.Cashless;
                }
                return null;
            }
        });
        multiplier.valueProperty().addListener(((observable, oldValue, newValue) -> {
            if(newValue != null) Main.character.inventory().setMode(newValue);
        }));
        addMoney.setOnAction(event -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Add Money");
            dialog.setHeaderText("Add Money (in sp)");
            dialog.setContentText("Amount:");
            dialog.showAndWait().ifPresent(s ->
                    Main.character.inventory().addMoney(Double.parseDouble(s)));
        });
    }

    private void refreshTotalValue() {
        value = 0;
        for (ItemCount itemCount : inventoryList) {
            value += itemCount.getCount() * itemCount.stats().getValue();
        }
        totalValue.setText(value+" sp");
    }

    private void setDisplay(Equipment selectedItem) {
        String s = EquipmentHTMLGenerator.generateText(selectedItem);
        itemDisplay.getEngine().loadContent(s);
//        System.out.println(s);
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

        if(Main.character.inventory().equip(unequippedItem.stats(), slot, 1)) {
            updateFromEquip(unequippedItem, slot);
        }
        controllerOp = false;
    }

    private void updateFromEquip(ItemCount unequippedItem, Slot slot) {
        boolean alreadyEquipped = false;
        for (OPair<ItemCount, Slot> pair : equipList) {
            if(pair.first.get().stats() == unequippedItem.stats() && pair.second.get() == slot && pair.first.get().getCount() > 0) {
                alreadyEquipped = true;
                break;
            }
        }
        if(!alreadyEquipped) {
            OPair<ItemCount, Slot> pair = new OPair<>(new ItemCount(unequippedItem.stats(), 1), slot);
            equipList.add(pair);
        } else {
            ItemCount equippedItem = find(equipSort, unequippedItem.stats(), opair->opair.first.get());
            if (equippedItem != null) {
                equippedItem.add(1);
            }
        }
        unequippedItem.remove(1);
    }

    private void tryToUnequip(ItemCount equippedItem, Slot slot) {
        if(Main.character.inventory().unequip(equippedItem.stats(), slot,1)) {
            equippedItem.remove(1);
            ItemCount unequippedItem = unequipMap.get(equippedItem.stats());
            if(unequippedItem != null)
                unequippedItem.add(1);
            if(equippedItem.getCount() == 0)
                equipList.removeIf(pair->(pair.first.get() == equippedItem) && (pair.second.get() == slot));
            unequipped.refresh();
        }
    }

    private void tryToSell(ItemCount item) {
        if (!Main.character.inventory().sell(item.stats(), 1)) {
            new Alert(Alert.AlertType.INFORMATION, "Not Enough Items!").showAndWait();
        }
    }

    private <T> ItemCount find(List<T> list, Equipment stats, Function<T,ItemCount> converter) {
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

    private void tryToBuy(Equipment item) {
        if (!Main.character.inventory().buy(item, 1)) {
            new Alert(Alert.AlertType.INFORMATION, "Not Enough Money!").showAndWait();
        }else{
            inventory.refresh();
            unequipped.refresh();
            equipped.refresh();
        }
    }
}
