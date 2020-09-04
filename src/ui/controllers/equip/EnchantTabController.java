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
import model.CharacterManager;
import model.equipment.Item;
import model.equipment.ItemCount;
import model.equipment.runes.Rune;
import model.equipment.runes.runedItems.*;
import model.player.PC;
import model.util.Pair;
import ui.controls.equipment.ItemsList;
import ui.controls.lists.entries.ItemEntry;
import ui.html.EquipmentHTMLGenerator;

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
    private ObservableList<Item> itemsList = FXCollections.observableArrayList();
    private ObservableList<Item> runesList = FXCollections.observableArrayList();
    private Item selectedItem = null;
    private Item selectedRune = null;
    private PC character;

    @FXML
    private void initialize() {
        itemDisplay.getEngine().setUserStyleSheetLocation(getClass().getResource("/webview_style.css").toString());
        runeDisplay.getEngine().setUserStyleSheetLocation(getClass().getResource("/webview_style.css").toString());
        character = CharacterManager.getActive();
        for (Map.Entry<Item, ItemCount> entry : character.inventory().getItems().entrySet()) {
            int count = entry.getValue().getCount();
            addItem(entry.getKey(), count);
        }
        addButton.setOnAction(this::addRune);
        removeButton.setOnAction(this::removeRune);
        upgradeButton.setOnAction(this::upgradeRune);

        character.inventory().addBuySellWatcher((o, oldVal, newVal) -> {
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

    private void typeCheckRunedItem(Item i) {
        Runes<?> runes = Runes.getRunes(i);
        if(runes != null)
            setRunesList(runes.list());
    }

    private void addRune(ActionEvent actionEvent) {
        if(!(Rune.isRune(selectedRune))) {
            notifyFail("add rune.", "Selected Rune not a rune.");
            return;
        }
        Pair<Boolean, Item> item = character.inventory().tryToAddRune(selectedItem, Rune.getRune(selectedRune));
        if(!item.first)
            notifyFail("add rune.", "Could not add rune.");
        select(item.second, itemsIL.getRoot());
        refreshLabels();
    }

    private void removeRune(ActionEvent actionEvent) {
        if(!(Rune.isRune(selectedRune))) {
            notifyFail("remove rune.", "Selected Rune not a rune.");
            return;
        }
        Pair<Boolean, Item> item = character.inventory().tryToRemoveRune(selectedItem, Rune.getRune(selectedRune));
        if(!item.first)
            notifyFail("remove rune.", "Could not remove rune.");
        select(item.second, itemsIL.getRoot());
        refreshLabels();
    }

    private void upgradeRune(ActionEvent actionEvent) {
        if(!(Rune.isRune(selectedRune))) {
            notifyFail("upgrade rune.", "Selected Rune not a rune.");
            return;
        }
        Rune upgradedRune = getUpgradedRune(selectedRune);
        if(upgradedRune == null) {
            notifyFail("remove rune.", "No rune to upgrade to.");
            return;
        }
        boolean result = character.inventory().tryToUpgradeRune(selectedItem, Rune.getRune(selectedRune), upgradedRune);
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

    private void addItem(Item item, int count) {
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

    private void setRunesList(ObservableList<Item> runes) {
        currentRunesIL = new ItemsList(runes, (item, count)->{
            clickRune(item, count);
            if(count == 2) {
                removeLabel.setText("Price: "+ generateCostString(item.getValue()*.1));
                Rune upgradedRune = getUpgradedRune(item);
                if(upgradedRune == null) {
                    upgradeLabel.setText("-Not Upgradeable-");
                    upgradePrice.setText("-Not Upgradeable-");
                }else{
                    upgradeLabel.setText(upgradedRune.getBaseItem().getName());
                    upgradePrice.setText("Price: "+ generateCostString(upgradedRune.getBaseItem().getValue() - item.getValue()));
                }
            }
        });
        currentRunesIL.removeColumn("cost");
        currentRunesIL.removeColumn("level");
        currentRunesIL.expandAll();
        currRunes.setCenter(currentRunesIL);
    }

    private Rune getUpgradedRune(Item item) {
        if(!(Rune.isRune(item))) return null;
        Rune rune = Rune.getRune(item);
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
        for (Item newRune : character.sources().equipment().getCategory("Runes").values()) {
            if(newRune.getName().matches(target)) {
                if(!(Rune.isRune(newRune))) continue;
                return Rune.getRune(newRune);
            }
        }
        return null;
    }

    private void clickRune(Item item, Integer count) {
        setRuneDisplay(item);
        if(count == 2) {
            selectedRune = item;
            currentRune.setText(selectedRune.getName());
        }
    }

    private void selectItem(Item item) {
        if(item == null) return;
        selectedItem = item;
        currentItem.setText(selectedItem.getName());
        Runes<?> runes = Runes.getRunes(item);
        if(runes == null) {
            propertyCount.setText("0/0 Property Runes");
        }else{
            propertyCount.setText(runes.getNumProperties() + "/" + runes.getMaxProperties()+" Property Runes");
        }
    }

    private void setItemDisplay(Item selectedItem) {
        if(selectedItem == null) return;
        String s = EquipmentHTMLGenerator.parse(selectedItem);
        itemDisplay.getEngine().loadContent(s);
    }

    private void setRuneDisplay(Item selectedItem) {
        if(selectedItem == null) return;
        String s = EquipmentHTMLGenerator.parse(selectedItem);
        runeDisplay.getEngine().loadContent(s);
    }

    private boolean select(Item item, TreeItem<ItemEntry> node) {
        selectItem(item);
        for (TreeItem<ItemEntry> child : node.getChildren()) {
            if(child.getValue().getContents() == item) {
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
