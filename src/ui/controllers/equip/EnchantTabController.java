package ui.controllers.equip;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import model.data_managers.EquipmentManager;
import model.equipment.Equipment;
import model.equipment.ItemCount;
import model.equipment.runes.Rune;
import model.equipment.runes.runedItems.*;
import model.util.Pair;
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
    private Label upgradeLabel, upgradePrice, removeLabel, currentItem, currentRune, propertyCount;
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
        upgradeButton.setOnAction(this::upgradeRune);

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
                selectItem(i);
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
        if(!(selectedRune instanceof Rune)) {
            notifyFail("add rune.", "Selected Rune not a rune.");
            return;
        }
        Pair<Boolean, Equipment> item = Main.character.inventory().tryToAddRune(selectedItem, (Rune) selectedRune);
        if(!item.first)
            notifyFail("add rune.", "Could not add rune.");
        select(item.second, itemsIL.getRoot());
        refreshLabels();
    }

    private void removeRune(ActionEvent actionEvent) {
        if(!(selectedRune instanceof Rune)) {
            notifyFail("remove rune.", "Selected Rune not a rune.");
            return;
        }
        Pair<Boolean, Equipment> item = Main.character.inventory().tryToRemoveRune(selectedItem, (Rune) selectedRune);
        if(!item.first)
            notifyFail("remove rune.", "Could not remove rune.");
        select(item.second, itemsIL.getRoot());
        refreshLabels();
    }

    private void upgradeRune(ActionEvent actionEvent) {
        if(!(selectedRune instanceof Rune)) {
            notifyFail("upgrade rune.", "Selected Rune not a rune.");
            return;
        }
        Rune upgradedRune = getUpgradedRune(selectedRune);
        if(upgradedRune == null) {
            notifyFail("remove rune.", "No rune to upgrade to.");
            return;
        }
        boolean result = Main.character.inventory().tryToUpgradeRune(selectedItem, (Rune) selectedRune, upgradedRune);
        if(!result) return;
        refreshLabels();
    }

    private void refreshLabels() {
        setItemDisplay(selectedItem);
        typeCheckRunedItem(selectedItem);
        selectedRune = null;
        selectItem(selectedItem);
        currentRune.setText("-No Rune Selected-");
        removeLabel.setText("-No Rune Selected-");
        upgradeLabel.setText("-No Rune Selected-");
        upgradePrice.setText("-No Rune Selected-");
    }

    private void addItem(Equipment item, int count) {
        if(count > 0) {
            String cat = item.getCategory();
            if(cat.equals("Weapon")
                    || cat.equals("Armor")
                    || cat.equals("Ranged Weapon")
                    || cat.equals("Shield")) {
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
                if(upgradedRune == null) {
                    upgradeLabel.setText("-Not Upgradeable-");
                    upgradePrice.setText("-Not Upgradeable-");
                }else{
                    upgradeLabel.setText(upgradedRune.getName());
                    upgradePrice.setText("Price: "+ generateCostString(upgradedRune.getValue() - item.getValue()));
                }
            }
        });
        currentRunesIL.removeColumn("cost");
        currentRunesIL.removeColumn("level");
        currentRunesIL.expandAll();
        currRunes.setCenter(currentRunesIL);
    }

    private Rune getUpgradedRune(Equipment item) {
        if(!(item instanceof Rune)) return null;
        Rune rune = (Rune) item;
        String currTier = rune.getTier();
        if(currTier.equals("Major") || currTier.equals("+3")) return null;
        String newTier;
        switch (currTier) {
            case "": newTier = "Greater";
                break;
            case "Greater": newTier = "Major";
                break;
            case "+1": newTier = "\\+2";
                break;
            case "+2": newTier = "\\+3";
            break;
            default: return null;
        }
        String target = rune.getBaseRune()+" ?\\("+newTier+"\\) ?";
        for (Equipment newRune : EquipmentManager.getItems("Runes")) {
            if(newRune.getName().matches(target)) {
                if(!(newRune instanceof Rune)) continue;
                return (Rune) newRune;
            }
        }
        return null;
    }

    private void clickRune(Equipment equipment, Integer count) {
        setRuneDisplay(equipment);
        if(count == 2) {
            selectedRune = equipment;
            currentRune.setText(selectedRune.getName());
        }
    }

    private void selectItem(Equipment item) {
        selectedItem = item;
        currentItem.setText(selectedItem.getName());
        if(!(selectedItem instanceof RunedEquipment)) {
            propertyCount.setText("0/0 Property Runes");
        }else{
            Runes runes = ((RunedEquipment) selectedItem).getRunes();
            propertyCount.setText(runes.getNumProperties() + "/" + runes.getMaxProperties()+" Property Runes");
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
        selectItem(item);
        for (TreeItem<ItemEntry> child : node.getChildren()) {
            if(child.getValue().getItem() == item) {
                itemsIL.getSelectionModel().select(child);
                return true;
            }else if(select(item, child)) return true;
        }
        return false;
    }

    private void notifyFail(String goal, String notice) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Failed to "+goal);
        alert.setContentText(notice);
        alert.show();
    }
}
