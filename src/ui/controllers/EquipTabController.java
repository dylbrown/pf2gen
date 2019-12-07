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
import model.data_managers.EquipmentManager;
import model.enums.ArmorProficiency;
import model.enums.Slot;
import model.equipment.*;
import model.util.OPair;
import ui.Main;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
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
    private ListView<Equipment> allItems;
    @FXML
    private ListView<ItemCount> inventory, unequipped;
    @FXML
    private TableView<OPair<ItemCount, Slot>> equipped;
    @FXML
    public GridPane itemGrid;
    @FXML
    private Label money, totalValue;

    @FXML
    private WebView item;
    @FXML
    private CheckMenuItem armorFilter, weaponFilter;
    @FXML
    private RadioMenuItem nameSort, priceSort, ascSort, descSort;
    private final ToggleGroup sortBy = new ToggleGroup(); private final ToggleGroup direction = new ToggleGroup();
    private double value = 0;

    //All Items
    private final ObservableList<Equipment> itemList = FXCollections.observableArrayList();
    private final FilteredList<Equipment> itemsFilter = new FilteredList<>(itemList);
    private final SortedList<Equipment> itemsSort = new SortedList<>(itemsFilter);

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
        nameCol.setCellValueFactory(param -> param.getValue().first.get().stats().nameProperty());
        slotCol.setCellValueFactory(param -> param.getValue().second);
        weightCol.setCellValueFactory(param -> param.getValue().first.get().stats().nameProperty());
        quantityCol.setCellValueFactory(param -> param.getValue().first.get().countProperty());


        itemList.addAll(EquipmentManager.getEquipment());
        money.setText(Main.character.inventory().getMoneyProperty().get()+" sp");
        Main.character.inventory().getMoneyProperty().addListener((event)-> money.setText(Main.character.inventory().getMoneyProperty().get()+" sp"));
        value = Main.character.inventory().getTotalValue();
        totalValue.setText(value+" sp");

        allItems.setItems(itemsSort);

        //Set Up Filter Toggles
        armorFilter.selectedProperty().addListener((change)->filterStore());
        weaponFilter.selectedProperty().addListener((change)->filterStore());

        //Set up sort options
        sortBy.getToggles().addAll(nameSort, priceSort);
        direction.getToggles().addAll(ascSort,descSort);
        sortBy.selectToggle(nameSort);
        sortBy.selectedToggleProperty().addListener((change)->sortStore());
        direction.selectedToggleProperty().addListener((change)->sortStore());
        direction.selectToggle(ascSort);



        allItems.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selectedItem) -> setDisplay(selectedItem));
        inventory.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selectedItem) -> {
            if(selectedItem != null) setDisplay(selectedItem.stats());
        });
        allItems.setOnMouseClicked((event) -> {
            if(event.getClickCount() % 2 == 0) {
                Equipment item = allItems.getSelectionModel().getSelectedItem();
                tryToBuy(item);
            }
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
    }

    private void refreshTotalValue() {
        value = 0;
        for (ItemCount itemCount : inventoryList) {
            value += itemCount.getCount() * itemCount.stats().getValue();
        }
        totalValue.setText(value+" sp");
    }

    private void setDisplay(Equipment selectedItem) {
        if (selectedItem instanceof Weapon)
            item.getEngine().loadContent(generateWeaponText((Weapon) selectedItem));
        else if (selectedItem instanceof Armor)
            item.getEngine().loadContent(generateArmorText((Armor) selectedItem));
    }

    private String generateArmorText(Armor armor) {
        StringBuilder text = new StringBuilder();
        text.append("<p><h3 style='display:inline;'>").append(armor.getName()).append("</h3><br>");
        if(armor.getProficiency() != ArmorProficiency.Shield)
            text.append(armor.getProficiency()).append(" Armor<br><b>Cost</b> ");
        else
            text.append("Shield<br><b>Cost</b> ");
        text.append(generateCostString(armor.getValue())).append("; <b>Bulk</b> ");
        text.append(armor.getPrettyWeight()).append("<br><b>AC Bonus</b> ");
        if(armor.getAC() >= 0)
            text.append("+");
        text.append(armor.getAC());
        if(armor instanceof Shield) {
            text.append("; <b>Speed Penalty</b> ");
            if (armor.getSpeedPenalty() < 0)
                text.append(Math.abs(armor.getSpeedPenalty())).append(" ft.");
            else
                text.append("—");
            text.append("<br><b>Hardness</b> ").append(((Shield) armor).getHardness());
            text.append("; <b>HP(BT)</b> ").append(((Shield) armor).getHP()).append("(");
            text.append(((Shield) armor).getBT()).append(")");
        }else{
            text.append("; <b>Dex Cap</b> +").append(armor.getMaxDex()).append("<br><b>ACP</b> ");
            if (armor.getACP() < 0)
                text.append(armor.getACP());
            else
                text.append("—");
            text.append("; <b>Speed Penalty</b> ");
            if (armor.getSpeedPenalty() < 0)
                text.append(Math.abs(armor.getSpeedPenalty())).append(" ft.");
            else
                text.append("—");
            text.append("<br><b>Strength</b> ");
            if (armor.getStrength() > 0)
                text.append(armor.getStrength());
            else
                text.append("—");
            text.append("; <b>Group</b> ").append(armor.getGroup().getName());
        }
        if(armor.getTraits().size() > 0)
            text.append("<br><b>Traits</b> ").append(armor.getTraits().stream().map(ItemTrait::getName).collect(Collectors.joining(", ")));
        return text.toString();
    }

    private String generateWeaponText(Weapon weapon) {
        StringBuilder text = new StringBuilder();
        text.append("<p><h3 style='display:inline;'>").append(weapon.getName()).append("</h3><br>");
        text.append(weapon.getProficiency().toString()).append(" Weapon<br><b>Cost</b> ");
        text.append(generateCostString(weapon.getValue())).append("; <b>Bulk</b> ");
        text.append(weapon.getPrettyWeight()).append("; <b>Hands</b> ").append(weapon.getHands());
        text.append("<br><b>Damage</b> ").append(weapon.getDamage()).append(" ").append(weapon.getDamageType().toString(), 0, 1).append("; <b>Group</b> ").append(weapon.getGroup().getName());
        if(weapon instanceof RangedWeapon){
            text.append("<br><b>Range</b> ").append(((RangedWeapon) weapon).getRange());
            text.append("; <b>Reload</b> ").append(((RangedWeapon) weapon).getReload());
        }
        if(weapon.getTraits().size() > 0)
            text.append("<br><b>Traits</b> ").append(weapon.getTraits().stream().map(ItemTrait::getName).collect(Collectors.joining(", ")));
        return text.toString();
    }

    private String generateCostString(double cost) {
        if(Math.floor(cost) != cost)
            return (int)(cost * 10) + " cp";
        else if(cost < 100 || Math.floor(cost/10) != cost/10)
            return (int)cost + " sp";
        else
            return (int)(cost / 10) + " gp";
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
            if (!result.isPresent()) {
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

    private void filterStore(){
        itemsFilter.setPredicate((item)-> (armorFilter.isSelected() && item instanceof Armor) || (weaponFilter.isSelected() && item instanceof Weapon));
    }

    private void sortStore() {
        Comparator<Equipment> comparator;
        if(sortBy.getSelectedToggle() == nameSort)
            comparator = Comparator.comparing(Equipment::toString);
        else
            comparator = Comparator.comparing(Equipment::getValue);
        if(direction.getSelectedToggle() == descSort)
            comparator = comparator.reversed();
        itemsSort.setComparator(comparator);
    }
}
