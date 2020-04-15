package ui.controllers.equip;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import model.equipment.Equipment;
import model.equipment.ItemCount;
import model.equipment.runes.Rune;
import model.equipment.runes.runedItems.RunedArmor;
import model.equipment.runes.runedItems.RunedRangedWeapon;
import model.equipment.runes.runedItems.RunedShield;
import model.equipment.runes.runedItems.RunedWeapon;
import ui.Main;
import ui.controls.equipment.EquipmentHTMLGenerator;
import ui.controls.equipment.lists.ItemEntry;
import ui.controls.equipment.lists.ItemsList;

import java.util.Map;

import static model.util.StringUtils.generateCostString;

public class EnchantTabController {
    @FXML
    private BorderPane weaponsAndArmor, runes, currRunes;
    private ItemsList itemsIL, runesIL, currentRunesIL;
    @FXML
    private Button upgradeButton, addButton, removeButton;
    @FXML
    private Label upgradeLabel, upgradePrice, removeLabel, currentWeapon, currentRune;
    @FXML
    private WebView itemDisplay, runeDisplay;
    private ObservableList<Equipment> itemsList = FXCollections.observableArrayList();
    private ObservableList<Equipment> runesList = FXCollections.observableArrayList();
    private Equipment selectedItem = null;
    private Equipment selectedRune = null;

    @FXML
    private void initialize() {
        for (Map.Entry<Equipment, ItemCount> entry : Main.character.inventory().getItems().entrySet()) {
            int count = entry.getValue().getCount();
            addItem(entry.getKey(), count);
        }
        addButton.setOnAction(this::addRune);
        removeButton.setOnAction(this::removeRune);

        //TODO: Add unmodifiableItemCount
        Main.character.inventory().addBuySellWatcher((o, oldVal, newVal) -> {
            int countChange = newVal.getCount() - oldVal.getCount();
            if(countChange < 0) {
                for(int i = 0; i > countChange; i--) {
                    itemsList.remove(oldVal.stats());
                    runesList.remove(oldVal.stats());
                }
            }else if(countChange > 0) {
                addItem(oldVal.stats(), countChange);
            }
        });
        itemsIL = new ItemsList(itemsList, (i, count) -> {
            setItemDisplay(i);
            if(count == 2) {
                typeCheckRunedItem(i);
                selectedItem = i;
                currentWeapon.setText(selectedItem.getName());
            }
        });
        weaponsAndArmor.setCenter(itemsIL);
        runesIL = new ItemsList(runesList, this::clickRune);
        runes.setCenter(runesIL);
    }

    private void typeCheckRunedItem(Equipment i) {
        if(i instanceof RunedArmor) setRunesList(((RunedArmor) i).getRunes().list());
        if(i instanceof RunedShield) setRunesList(((RunedShield) i).getRunes().list());
        if(i instanceof RunedWeapon) setRunesList(((RunedWeapon) i).getRunes().list());
        if(i instanceof RunedRangedWeapon) setRunesList(((RunedRangedWeapon) i).getRunes().list());
    }

    private void addRune(ActionEvent actionEvent) {
        if(!(selectedRune instanceof Rune)) return;
        Equipment item = Main.character.inventory().tryToAddRune(selectedItem, (Rune) selectedRune);
        if(item == null) return; // TODO: Add fail popup
        setItemDisplay(item);
        select(item, itemsIL.getRoot());
        typeCheckRunedItem(item);
        selectedRune = null;
        currentWeapon.setText(selectedItem.getName());
        currentRune.setText("-No Rune Selected-");
    }

    private void removeRune(ActionEvent actionEvent) {
        if(!(selectedRune instanceof Rune)) return;
        Equipment item = Main.character.inventory().tryToRemoveRune(selectedItem, (Rune) selectedRune);
        if(item == null) return; // TODO: Add fail popup
        setItemDisplay(item);
        select(item, itemsIL.getRoot());
        typeCheckRunedItem(item);
        selectedRune = null;
        currentWeapon.setText(selectedItem.getName());
        currentRune.setText("-No Rune Selected-");
        removeLabel.setText("-No Rune Selected-");
        upgradeLabel.setText("-No Rune Selected-");
        upgradePrice.setText("-No Rune Selected-");
    }

    private void addItem(Equipment item, int count) {
        if(count > 0) {
            String cat = item.getCategory();
            if(cat.equals("Weapon") || cat.equals("Armor")) {
                for(int i = 0; i < count; i++)
                    itemsList.add(item);
            }else if (cat.equals("Runes")) {
                for(int i = 0; i < count; i++)
                    runesList.add(item);
            }
        }
    }

    private void setRunesList(ObservableList<Equipment> runes) {
        currentRunesIL = new ItemsList(runes, (item, count)->{
            clickRune(item, count);
            if(count == 2) {
                removeLabel.setText("Price: "+ generateCostString(item.getValue()*.1));
                Equipment upgradedRune = getUpgradedRune(item);
                upgradeLabel.setText(upgradedRune.getName());
                upgradePrice.setText("Price: "+ generateCostString(upgradedRune.getValue() - item.getValue()));
            }
        });
        currentRunesIL.removeColumn("cost");
        currentRunesIL.removeColumn("level");
        currRunes.setCenter(currentRunesIL);
    }

    private Equipment getUpgradedRune(Equipment item) {
        // TODO: implement
        return item;
    }

    private void clickRune(Equipment equipment, Integer count) {
        setRuneDisplay(equipment);
        if(count == 2) {
            selectedRune = equipment;
            currentRune.setText(selectedRune.getName());
        }
    }

    private void setItemDisplay(Equipment selectedItem) {
        String s = EquipmentHTMLGenerator.generateText(selectedItem);
        itemDisplay.getEngine().loadContent(s);
    }

    private void setRuneDisplay(Equipment selectedItem) {
        String s = EquipmentHTMLGenerator.generateText(selectedItem);
        runeDisplay.getEngine().loadContent(s);
    }

    private boolean select(Equipment item, TreeItem<ItemEntry> node) {
        selectedItem = item;
        currentWeapon.setText(selectedItem.getName());
        for (TreeItem<ItemEntry> child : node.getChildren()) {
            if(child.getValue().getItem() == item) {
                itemsIL.getSelectionModel().select(child);
                return true;
            }else if(select(item, child)) return true;
        }
        return false;
    }
}
