package ui.controllers.equip;

import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.util.StringConverter;
import model.CharacterManager;
import model.enums.BuySellMode;
import model.enums.Slot;
import model.items.BaseItem;
import model.items.BaseItemChoices;
import model.items.Item;
import model.items.ItemCount;
import model.player.PC;
import model.util.StringUtils;
import ui.controls.Popup;
import ui.controls.equipment.CategoryAllItemsList;
import ui.controls.equipment.EquippedEntry;
import ui.controls.equipment.LevelAllItemsList;
import ui.controls.lists.entries.ItemEntry;
import ui.html.EquipmentHTMLGenerator;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static model.util.StringUtils.generateCostString;

public class EquipTabController {

    public RadioMenuItem anyLevel, yourLevelLower;
    @FXML
    private TableColumn<EquippedEntry, String> nameCol,weightCol;
    @FXML
    private TableColumn<EquippedEntry, Slot> slotCol;
    @FXML
    private TableColumn<EquippedEntry, Integer> quantityCol;
    @FXML
    private BorderPane allItemsContainer;
    @FXML
    private TextField search;
    private CategoryAllItemsList categoryGroup;
    private LevelAllItemsList levelGroup;
    @FXML
    private ListView<ItemCount> inventory, unequipped;
    @FXML
    private TableView<EquippedEntry> equipped;
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
    private RadioMenuItem groupByCategory, groupByLevel;
    @FXML
    private ToggleGroup levelFilter, groupBy;
    private double value = 0;

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
        character = CharacterManager.getActive();
        categoryGroup = new CategoryAllItemsList(character, this::tryToBuy);
        levelGroup = new LevelAllItemsList(character, this::tryToBuy);
        // itemDisplay.setZoom(1.25);
        nameCol.setCellValueFactory(param -> param.getValue().nameProperty());
        slotCol.setCellValueFactory(param -> param.getValue().slotProperty());
        weightCol.setCellValueFactory(param -> param.getValue().weightProperty());
        quantityCol.setCellValueFactory(param -> param.getValue().countProperty());
        equipped.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        for (TableColumn<EquippedEntry, ?> column : equipped.getColumns()) {
            column.setReorderable(false);
        }
        allItemsContainer.setCenter(categoryGroup);
        groupByCategory.setOnAction(event->allItemsContainer.setCenter(categoryGroup));
        groupByLevel.setOnAction(event->allItemsContainer.setCenter(levelGroup));

        ReadOnlyObjectWrapper<Predicate<ItemEntry>> wrapper = new ReadOnlyObjectWrapper<>();

        Runnable resetFilter = () -> {
            boolean filterLevel = !levelFilter.getSelectedToggle().equals(anyLevel);
            if (search.getText().length() == 0 && !filterLevel)
                wrapper.set(null);
            else
                wrapper.set(itemEntry -> StringUtils.containsIgnoreCase(itemEntry.toString(), search.getText())
                        && (!filterLevel || (itemEntry.getContents() != null &&
                        itemEntry.getContents().getLevel() <= character.getLevel())));
        };
        resetFilter.run();
        levelFilter.selectedToggleProperty().addListener(c->resetFilter.run());
        search.textProperty().addListener(c->resetFilter.run());
        character.levelProperty().addListener(c->resetFilter.run());

        categoryGroup.setFilter(wrapper.getReadOnlyProperty());
        levelGroup.setFilter(wrapper.getReadOnlyProperty());

        money.setText(generateCostString(character.inventory().getMoneyProperty().get()));
        character.inventory().getMoneyProperty().addListener((event)->
                money.setText(generateCostString(character.inventory().getMoneyProperty().get())));
        value = character.inventory().getTotalValue();
        totalValue.setText(value+" sp");

        //Set up sort options

        ChangeListener<TreeItem<ItemEntry>> listener = (obs, oldVal, selectedItem) -> {
            if(selectedItem == null || selectedItem.getValue() == null) return;
            Item item = selectedItem.getValue().getContents();
            if (item != null) setDisplay(item);
        };

        categoryGroup.getSelectionModel().selectedItemProperty().addListener(listener);
        levelGroup.getSelectionModel().selectedItemProperty().addListener(listener);
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

        inventory.setItems(new SortedList<>(inventoryList, Comparator.comparing(o -> o.stats().toString())));
        unequipped.setItems(unequipSort);
        equipped.setItems(equipFilter);

        character.inventory().addInventoryListener(change -> {
            if(change.wasRemoved()) {
                ItemCount valueRemoved = change.getValueRemoved();
                inventoryList.remove(valueRemoved);
                unequipList.removeIf(item->item.stats() == valueRemoved.stats());
                equipList.removeIf(pair -> pair.getItemCount() == valueRemoved);
            }
            if(change.wasAdded()) {
                ItemCount valueAdded = change.getValueAdded();
                addHandler(valueAdded);
            }
            refreshTotalValue();
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

        multiplier.getItems().addAll(BuySellMode.values());
        multiplier.setConverter(new StringConverter<>() {
            @Override
            public String toString(BuySellMode object) {
                switch (object) {
                    case FullPrice:
                        return "Full Price";
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
                    case "FullPrice":
                        return BuySellMode.FullPrice;
                    case "Sell Half":
                        return BuySellMode.SellHalf;
                    case "Cashless":
                        return BuySellMode.Cashless;
                }
                return null;
            }
        });
        multiplier.valueProperty().addListener(((observable, oldValue, newValue) -> {
            if(newValue != null) character.inventory().setMode(newValue);
        }));
        addMoney.setOnAction(event -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Add Money");
            dialog.setHeaderText("Add Money (in sp)");
            dialog.setContentText("Amount:");
            dialog.showAndWait().ifPresent(s ->
                    character.inventory().addMoney(Double.parseDouble(s)));
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

    private void refreshTotalValue() {
        value = 0;
        for (ItemCount itemCount : inventoryList) {
            value += itemCount.getCount() * itemCount.stats().getValue();
        }
        totalValue.setText(value+" sp");
    }

    private void setDisplay(Item selectedItem) {
        String s = EquipmentHTMLGenerator.parse(selectedItem);
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

    private void tryToSell(ItemCount item) {
        if (!character.inventory().sell(item.stats(), 1)) {
            new Alert(Alert.AlertType.INFORMATION, "Not Enough Items!").showAndWait();
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

    private boolean tryToBuy(Item item, int count) {
        if(count != 2) return false;
        if(item instanceof BaseItem && item.hasExtension(BaseItemChoices.class)) {
            CustomItemController controller = new CustomItemController((BaseItem) item, i->tryToBuy(i, 2));
            Popup.popup("/fxml/equip/customItem.fxml", controller, Modality.NONE);
            return false;
        }
        if (!character.inventory().buy(item, 1)) {
            new Alert(Alert.AlertType.INFORMATION, "Not Enough Money!").showAndWait();
            return false;
        }else{
            inventory.refresh();
            unequipped.refresh();
            equipped.refresh();
            return true;
        }
    }
}
