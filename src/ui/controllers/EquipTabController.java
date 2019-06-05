package ui.controllers;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import model.data_managers.EquipmentManager;
import model.equipment.Armor;
import model.equipment.Equipment;
import model.equipment.Weapon;
import ui.Main;

import java.util.Comparator;

public class EquipTabController {

    @FXML
    public Label TotalMoney;
    @FXML
    private Label itemName, itemWeight, itemCost, itemRarity, itemDesc, itemDamage, itemHands, itemGroup;
    @FXML
    private ListView<Equipment> inventory, allItems, unequipped;
    @FXML
    private TableView<Equipment> equipped;
    @FXML
    public GridPane itemGrid;
    @FXML
    private Label itemAC, itemTAC, itemMaxDex, itemACP, itemSpeedPenalty, money, totalValue;
    @FXML
    private HBox itemInfoRow;
    private double value = 0;
    private final ReadOnlyObjectWrapper<Boolean> weaponShowing = new ReadOnlyObjectWrapper<>(true);
    private final ReadOnlyObjectWrapper<Boolean> armorShowing = new ReadOnlyObjectWrapper<>(true);
    private final ObservableList<Node> simpleItemRow = FXCollections.observableArrayList();
    private final ObservableList<Node> weaponRow = FXCollections.observableArrayList();
    private final ObservableList<Node> armorRow = FXCollections.observableArrayList();
    private final ObservableList<Equipment> inventoryList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        allItems.getItems().addAll(EquipmentManager.getEquipment());
        money.setText(Main.character.inventory().getMoneyProperty().get()+" sp");
        Main.character.inventory().getMoneyProperty().addListener((event)-> money.setText(Main.character.inventory().getMoneyProperty().get()+" sp"));
        value = Main.character.inventory().getTotalValue();
        totalValue.setText(value+" sp");

        FXCollections.sort(allItems.getItems(), Comparator.comparing(Equipment::toString));
        int i=0;
        ObservableList<Node> nodes = itemInfoRow.getChildren();
        for (Node node : nodes) {
            if(i > 2 && i <= 7) {
                armorRow.add(node);
            }else if(i > 7) {
                weaponRow.add(node);
            }else{
                armorRow.add(node);
                weaponRow.add(node);
                simpleItemRow.add(node);
            }
            i++;
        }


        ChangeListener<Equipment> displayItem = (obs, oldVal, selectedItem) ->{
            if(selectedItem == null) return;
            itemName.setText(selectedItem.getName());
            itemWeight.setText(selectedItem.getPrettyWeight());
            double value = selectedItem.getValue();
            long round = Math.round(value);
            if(value-round == 0)
                itemCost.setText(round+" sp");
            else
                itemCost.setText(value+" sp");
            itemRarity.setText(selectedItem.getRarity().toString());
            itemDesc.setText(selectedItem.getDesc());
            if(selectedItem instanceof Weapon) {
                itemDamage.setText(((Weapon) selectedItem).getDamage().toString());
                itemHands.setText(String.valueOf(((Weapon) selectedItem).getHands()));
                itemGroup.setText(((Weapon) selectedItem).getGroup().getName());
                armorShowing.set(false);
                weaponShowing.set(true);
                itemInfoRow.getChildren().setAll(weaponRow);
            }else if (selectedItem instanceof Armor){
                itemAC.setText(String.valueOf(((Armor) selectedItem).getAC()));
                itemTAC.setText(String.valueOf(((Armor) selectedItem).getTAC()));
                itemMaxDex.setText(String.valueOf(((Armor) selectedItem).getMaxDex()));
                itemACP.setText(String.valueOf(((Armor) selectedItem).getACP()));
                itemSpeedPenalty.setText(((Armor) selectedItem).getSpeedPenalty() +" ft.");
                armorShowing.set(true);
                weaponShowing.set(false);
                itemInfoRow.getChildren().setAll(armorRow);
            }else {
                armorShowing.set(false);
                weaponShowing.set(false);
                itemInfoRow.getChildren().setAll(simpleItemRow);
            }
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
        armorShowing.set(false);
        weaponShowing.set(false);
        itemInfoRow.getChildren().setAll(simpleItemRow);

        inventory.setItems(new SortedList<>(inventoryList, Comparator.comparing(Equipment::toString)));

        Main.character.inventory().addInventoryListener(change -> {
            if(change.wasRemoved()) {
                inventoryList.remove(change.getKey());
                unequipped.getItems().remove(change.getKey());
                value -= change.getKey().getValue();
                totalValue.setText(value+" sp");
            }
            if(change.wasAdded()) {
                inventoryList.add(change.getKey());
                unequipped.getItems().add(change.getKey());
                value += change.getKey().getValue();
                totalValue.setText(value+" sp");
                equipped.getItems().remove(change.getKey());
            }
        });
    }

    private void tryToEquip(Equipment item) {
        if(Main.character.inventory().equip(item, 1)) {
            if(!equipped.getItems().contains(item))
                equipped.getItems().add(item);
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
}
