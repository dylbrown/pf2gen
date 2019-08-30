package ui.controllers;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.web.WebView;
import model.data_managers.EquipmentManager;
import model.enums.ArmorProficiency;
import model.equipment.*;
import ui.Main;

import java.util.Comparator;
import java.util.stream.Collectors;

public class EquipTabController {

    @FXML
    private ListView<Equipment> inventory, allItems, unequipped;
    @FXML
    private TableView<Equipment> equipped;
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
    ToggleGroup sortBy = new ToggleGroup(); ToggleGroup direction = new ToggleGroup();
    private double value = 0;
    private final ObservableList<Equipment> items = FXCollections.observableArrayList();
    private final FilteredList<Equipment> filter = new FilteredList<>(items);
    private final SortedList<Equipment> sort = new SortedList<>(filter);
    private final ObservableList<Equipment> inventoryList = FXCollections.observableArrayList();
    private final ObservableList<Equipment> unequipList = FXCollections.observableArrayList();
    private final ObservableList<Equipment> equipList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        items.addAll(EquipmentManager.getEquipment());
        money.setText(Main.character.inventory().getMoneyProperty().get()+" sp");
        Main.character.inventory().getMoneyProperty().addListener((event)-> money.setText(Main.character.inventory().getMoneyProperty().get()+" sp"));
        value = Main.character.inventory().getTotalValue();
        totalValue.setText(value+" sp");

        allItems.setItems(sort);

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


        ChangeListener<Equipment> displayItem = (obs, oldVal, selectedItem) ->{
            if(selectedItem instanceof Weapon)
                item.getEngine().loadContent(generateWeaponText((Weapon)selectedItem));
            else if(selectedItem instanceof Armor)
                item.getEngine().loadContent(generateArmorText((Armor) selectedItem));
        };
        allItems.getSelectionModel().selectedItemProperty().addListener(displayItem);
        inventory.getSelectionModel().selectedItemProperty().addListener(displayItem);
        allItems.setOnMouseClicked((event) -> {
            if(event.getClickCount() == 2) {
                Equipment item = allItems.getSelectionModel().getSelectedItem();
                tryToBuy(item);
            }
        });
        inventory.setOnMouseClicked((event) -> {
            if(event.getClickCount() == 2) {
                Equipment item = inventory.getSelectionModel().getSelectedItem();
                tryToSell(item);
            }
        });
        unequipped.setOnMouseClicked((event) -> {
            if(event.getClickCount() == 2) {
                Equipment item = unequipped.getSelectionModel().getSelectedItem();
                tryToEquip(item);
            }
        });

        equipped.setOnMouseClicked(event -> {
            if(event.getClickCount() == 2) {
                Equipment item = equipped.getSelectionModel().getSelectedItem();
                tryToUnequip(item);
            }
        });

        inventory.setItems(new SortedList<>(inventoryList, Comparator.comparing(Equipment::toString)));
        unequipped.setItems(new SortedList<>(unequipList, Comparator.comparing(Equipment::toString)));
        equipped.setItems(new SortedList<>(equipList, Comparator.comparing(Equipment::toString)));

        Main.character.inventory().addInventoryListener(change -> {
            if(change.wasRemoved()) {
                inventoryList.remove(change.getKey());
                unequipList.remove(change.getKey());
                value -= change.getKey().getValue();
                totalValue.setText(value+" sp");
                equipList.remove(change.getKey());
            }
            if(change.wasAdded()) {
                inventoryList.add(change.getKey());
                unequipList.add(change.getKey());
                value += change.getKey().getValue();
                totalValue.setText(value+" sp");
            }
        });
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
            return cost * 10 + " cp";
        else if(Math.floor(cost/10) != cost/10)
            return cost + " sp";
        else
            return cost / 10 + " gp";
    }

    private void tryToEquip(Equipment item) {
        if(Main.character.inventory().equip(item, 1)) {
            if(!equipList.contains(item))
                equipList.add(item);
            unequipList.remove(item);
        }
    }

    private void tryToUnequip(Equipment item) {
        if(Main.character.inventory().unequip(item, 1)) {
            if(!unequipList.contains(item))
                unequipList.add(item);
            equipList.remove(item);
        }
    }

    private void tryToSell(Equipment item) {

        if (!Main.character.inventory().sell(item, 1)) {
            new Alert(Alert.AlertType.INFORMATION, "Not Enough Items!").showAndWait();
        }
    }

    private void tryToBuy(Equipment item) {
        if (!Main.character.inventory().buy(item, 1)) {
            new Alert(Alert.AlertType.INFORMATION, "Not Enough Money!").showAndWait();
        }
    }

    private void filterStore(){
        filter.setPredicate((item)-> (armorFilter.isSelected() && item instanceof Armor) || (weaponFilter.isSelected() && item instanceof Weapon));
    }

    private void sortStore() {
        Comparator<Equipment> comparator;
        if(sortBy.getSelectedToggle() == nameSort)
            comparator = Comparator.comparing(Equipment::toString);
        else
            comparator = Comparator.comparing(Equipment::getValue);
        if(direction.getSelectedToggle() == descSort)
            comparator = comparator.reversed();
        sort.setComparator(comparator);
    }
}
