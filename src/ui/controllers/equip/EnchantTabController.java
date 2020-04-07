package ui.controllers.equip;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import model.equipment.Equipment;
import model.equipment.ItemCount;
import ui.Main;
import ui.controls.equipment.lists.ItemsList;

import java.util.Map;

public class EnchantTabController {
    @FXML
    private BorderPane weaponsAndArmor, runes;
    private ObservableList<Equipment> itemsList = FXCollections.observableArrayList();
    private ObservableList<Equipment> runesList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        for (Map.Entry<Equipment, ItemCount> entry : Main.character.inventory().getItems().entrySet()) {
            int count = entry.getValue().getCount();
            addItem(entry.getKey(), count);
        }

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
        weaponsAndArmor.setCenter(new ItemsList(itemsList, (i) -> {

        }));
        runes.setCenter(new ItemsList(runesList, (i) -> {

        }));
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
}
