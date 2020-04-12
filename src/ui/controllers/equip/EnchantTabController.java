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
import model.equipment.armor.Armor;
import model.equipment.runes.ArmorRune;
import model.equipment.runes.Rune;
import model.equipment.runes.WeaponRune;
import model.equipment.runes.runedItems.RunedArmor;
import model.equipment.runes.runedItems.RunedWeapon;
import model.equipment.weapons.Weapon;
import ui.Main;
import ui.controls.equipment.EquipmentHTMLGenerator;
import ui.controls.equipment.lists.ItemEntry;
import ui.controls.equipment.lists.ItemsList;

import java.util.Map;

public class EnchantTabController {
    @FXML
    private BorderPane weaponsAndArmor, runes, currRunes;
    private ItemsList itemsIL, runesIL, currentRunesIL;
    @FXML
    private Button upgradeButton, addButton, removeButton;
    @FXML
    private Label upgradeLabel, removeLabel, currentWeapon, currentRune;
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
                if(i instanceof RunedArmor) setRunesList((RunedArmor) i);
                if(i instanceof RunedWeapon) setRunesList((RunedWeapon) i);
                selectedItem = i;
                currentWeapon.setText(selectedItem.getName());
            }
        });
        weaponsAndArmor.setCenter(itemsIL);
        runesIL = new ItemsList(runesList, (i, count) -> {
            setRuneDisplay(i);
            if(count == 2) {
                selectedRune = i;
                currentRune.setText(selectedRune.getName());
            }
        });
        runes.setCenter(runesIL);
    }

    private void addRune(ActionEvent actionEvent) {
        if(!(selectedRune instanceof Rune)) return;
        if(selectedRune instanceof ArmorRune) {
            if (selectedItem instanceof RunedArmor) {
                Main.character.inventory().tryToAddRune((RunedArmor) selectedItem, (ArmorRune) selectedRune);
            }else if(selectedItem instanceof Armor) {
                if(!((ArmorRune) selectedRune).isFundamental()) return;
                RunedArmor runedArmor = Main.character.inventory().convertToRuned((Armor) selectedItem);
                Main.character.inventory().tryToAddRune(runedArmor, (ArmorRune) selectedRune);
                setItemDisplay(runedArmor);
                setRunesList(runedArmor);
                select(runedArmor, itemsIL.getRoot());
            }
        } else if (selectedRune instanceof WeaponRune) {
            if (selectedItem instanceof RunedWeapon) {
                Main.character.inventory().tryToAddRune((RunedWeapon) selectedItem, (WeaponRune) selectedRune);
            }else if(selectedItem instanceof Weapon) {
                if(!((WeaponRune) selectedRune).isFundamental()) return;
                RunedWeapon runedWeapon = Main.character.inventory().convertToRuned((Weapon) selectedItem);
                Main.character.inventory().tryToAddRune(runedWeapon, (WeaponRune) selectedRune);
                setRuneDisplay(runedWeapon);
                setRunesList(runedWeapon);
                select(runedWeapon, itemsIL.getRoot());
            }
        }
        currentWeapon.setText(selectedItem.getName());
        currentRune.setText("-No Rune Selected-");
    }

    private void removeRune(ActionEvent actionEvent) {

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

    private void setRunesList(RunedArmor e) {
        currentRunesIL = new ItemsList(e.getRunes().list(), this::clickRune);
        currentRunesIL.removeColumn("cost");
        currentRunesIL.removeColumn("level");
        currRunes.setCenter(currentRunesIL);
    }

    private void setRunesList(RunedWeapon e) {
        currentRunesIL = new ItemsList(e.getRunes().list(), this::clickRune);
        currentRunesIL.removeColumn("cost");
        currentRunesIL.removeColumn("level");
        currRunes.setCenter(currentRunesIL);
    }

    private void clickRune(Equipment equipment, Integer integer) {
        setRuneDisplay(equipment);
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
