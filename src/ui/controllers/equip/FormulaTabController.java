package ui.controllers.equip;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.layout.AnchorPane;
import model.CharacterManager;
import model.items.Item;
import model.items.ItemFormula;
import model.player.PC;
import ui.controls.equipment.CategoryAllItemsList;
import ui.controls.equipment.LevelAllItemsList;
import ui.controls.lists.ObservableEntryList;
import ui.controls.lists.entries.ItemEntry;
import ui.controls.lists.factories.TreeCellFactory;

import java.util.Arrays;
import java.util.List;

public class FormulaTabController extends PurchaseTabController {
    @FXML
    private AnchorPane inventoryContainer;
    private ObservableEntryList<Item, ItemEntry> inventory;
    PC character;
    @FXML
    @Override
    protected void initialize() {
        character = CharacterManager.getActive();
        inventory = ObservableEntryList.makeList(
                character.inventory().getFormulas(),
                this::handleClick,
                item-> "Level " + item.getLevel(),
                ItemEntry::new,
                ItemEntry::new,
                this::makeColumns);
        inventoryContainer.getChildren().add(inventory);
        AnchorPane.setLeftAnchor(inventory, 0.0);
        AnchorPane.setRightAnchor(inventory, 0.0);
        AnchorPane.setTopAnchor(inventory, 0.0);
        AnchorPane.setBottomAnchor(inventory, 0.0);
        super.initialize();
    }

    @Override
    protected boolean tryToBuy(Item item, int count) {
        if(count != 2) return false;
        if (!character.inventory().addFormula(item)) {
            new Alert(Alert.AlertType.INFORMATION, "Not Enough Money!").showAndWait();
            return false;
        }else{
            return true;
        }
    }

    private void handleClick(Item item, int count) {
        setDisplay(item);
        if(count != 2) return;
        character.inventory().removeFormula(item);
    }

    private List<TreeTableColumn<ItemEntry, ?>> makeColumns(ReadOnlyDoubleProperty width) {
        TreeTableColumn<ItemEntry, String> name = new TreeTableColumn<>("Name");
        TreeTableColumn<ItemEntry, String> level = new TreeTableColumn<>("Level");
        name.setCellValueFactory(new TreeCellFactory<>("name"));
        name.minWidthProperty().bind(width.multiply(.6));
        level.setCellValueFactory(new TreeCellFactory<>("level"));
        level.setStyle( "-fx-alignment: CENTER;");
        level.setComparator((s1,s2)->{
            double d1 = (!s1.equals(""))? Double.parseDouble(s1) : 0;
            double d2 = (!s2.equals(""))? Double.parseDouble(s2) : 0;
            return Double.compare(d1, d2);
        });
        return Arrays.asList(name, level);
    }

    @Override
    protected void initializeShop() {
        categoryGroup = new CategoryAllItemsList(character, this::tryToBuy, ItemFormula::new);
        levelGroup = new LevelAllItemsList(character, this::tryToBuy, ItemFormula::new);
    }
}
