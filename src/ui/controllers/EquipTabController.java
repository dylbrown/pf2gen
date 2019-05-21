package ui.controllers;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import model.FileLoader;
import model.equipment.Armor;
import model.equipment.Equipment;
import model.equipment.Weapon;
import ui.Main;

import java.util.Comparator;

public class EquipTabController {

    public Label TotalMoney;
    public Label itemName;
    public Label itemWeight;
    public Label itemCost;
    public Label itemRarity;
    public Label itemDesc;
    public Label itemDamage;
    public Label itemHands;
    public Label itemGroup;
    public ListView<Equipment> inventory;
    public ListView<Equipment> allItems;
    public ListView<Equipment> unequipped;
    public TableView<Equipment> equipped;
    public GridPane itemGrid;
    public Label itemAC;
    public Label itemTAC;
    public Label itemMaxDex;
    public Label itemACP;
    public Label itemSpeedPenalty;
    public HBox itemInfoRow;
    public Label money;
    public Label totalValue;
    private double value = 0;
    private ReadOnlyObjectWrapper<Boolean> weaponShowing = new ReadOnlyObjectWrapper<>(true);
    private ReadOnlyObjectWrapper<Boolean> armorShowing = new ReadOnlyObjectWrapper<>(true);
    private ObservableList<Node> simpleItemRow = FXCollections.observableArrayList();
    private ObservableList<Node> weaponRow = FXCollections.observableArrayList();
    private ObservableList<Node> armorRow = FXCollections.observableArrayList();
    private ObservableList<Equipment> inventoryList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        allItems.getItems().addAll(FileLoader.getWeapons());
        allItems.getItems().addAll(FileLoader.getArmorAndShields());
        money.setText(Main.character.getMoneyProperty().get()+" sp");
        Main.character.getMoneyProperty().addListener((event)-> money.setText(Main.character.getMoneyProperty().get()+" sp"));
        value = Main.character.getTotalValue();
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
    }

    private void tryToEquip(Equipment item) {
        if(Main.character.equip(item, 1)) {
            if(!equipped.getItems().contains(item))
                equipped.getItems().add(item);
        }
    }

    private void tryToSell(Equipment item) {

        if(Main.character.sell(item, 1)) {
            if(Main.character.getCount(item) == 0) {
                unequipped.getItems().remove(item);
                inventoryList.remove(item);
                equipped.getItems().remove(item);
            }
            value -= item.getValue();
            totalValue.setText(value+" sp");
        }else{
            new Alert(Alert.AlertType.INFORMATION, "Not Enough Items!").showAndWait();
        }
    }

    private void tryToBuy(Equipment item) {
        if(Main.character.buy(item, 1)) {
            if(!unequipped.getItems().contains(item)) {
                unequipped.getItems().add(item);
            }
            if(!inventoryList.contains(item)){
                inventoryList.add(item);
            }
            value += item.getValue();
            totalValue.setText(value+" sp");
        }else{
            new Alert(Alert.AlertType.INFORMATION, "Not Enough Money!").showAndWait();
        }
    }
}
