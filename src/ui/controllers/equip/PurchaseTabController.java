package ui.controllers.equip;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.util.StringConverter;
import model.CharacterManager;
import model.enums.BuySellMode;
import model.items.BaseItem;
import model.items.BaseItemChoices;
import model.items.Item;
import model.items.ItemCount;
import model.player.PC;
import model.util.StringUtils;
import ui.controls.Popup;
import ui.controls.equipment.CategoryAllItemsList;
import ui.controls.equipment.LevelAllItemsList;
import ui.controls.lists.entries.ItemEntry;
import ui.html.EquipmentHTMLGenerator;

import java.util.Comparator;
import java.util.function.Predicate;

import static model.util.StringUtils.generateCostString;

public class PurchaseTabController {
    @FXML
    private BorderPane allItemsContainer;
    @FXML
    private TextField search;
    @FXML
    private ListView<ItemCount> inventory;
    @FXML
    public GridPane itemGrid;
    @FXML
    private Label money, totalValue;
    @FXML
    private ComboBox<BuySellMode> multiplier;
    @FXML
    private Button addMoney;
    @FXML
    private WebView itemDisplay;
    @FXML
    private RadioMenuItem groupByCategory, groupByLevel, anyLevel, yourLevelLower;
    @FXML
    private ToggleGroup levelFilter, groupBy;
    protected CategoryAllItemsList categoryGroup;
    protected LevelAllItemsList levelGroup;
    private double value = 0;
    private PC character;

    @FXML
    protected void initialize() {
        character = CharacterManager.getActive();
        initializeShop();
        allItemsContainer.setCenter(categoryGroup);
        groupByCategory.setOnAction(event->allItemsContainer.setCenter(categoryGroup));
        groupByLevel.setOnAction(event->allItemsContainer.setCenter(levelGroup));

        ReadOnlyObjectWrapper<Predicate<ItemEntry>> wrapper = new ReadOnlyObjectWrapper<>();

        Runnable resetFilter = () -> {
            boolean filterLevel = !levelFilter.getSelectedToggle().equals(anyLevel);
            if (search.getText().length() == 0 && !filterLevel)
                wrapper.set(null);
            else
                wrapper.set(itemEntry -> StringUtils.containsIgnoreCase(itemEntry.toString(), search.getText())
                        && (!filterLevel || (itemEntry.getContents() != null &&
                        itemEntry.getContents().getLevel() <= character.getLevel())));
        };
        resetFilter.run();
        levelFilter.selectedToggleProperty().addListener(c->resetFilter.run());
        search.textProperty().addListener(c->resetFilter.run());
        character.levelProperty().addListener(c->resetFilter.run());

        categoryGroup.setFilter(wrapper.getReadOnlyProperty());
        levelGroup.setFilter(wrapper.getReadOnlyProperty());

        money.setText(generateCostString(character.inventory().getMoneyProperty().get()));
        character.inventory().getMoneyProperty().addListener((event)->
                money.setText(generateCostString(character.inventory().getMoneyProperty().get())));
        value = character.inventory().getTotalValue();
        totalValue.setText(value+" sp");

        //Set up sort options

        ChangeListener<TreeItem<ItemEntry>> listener = (obs, oldVal, selectedItem) -> {
            if(selectedItem == null || selectedItem.getValue() == null) return;
            Item item = selectedItem.getValue().getContents();
            if (item != null) setDisplay(item);
        };

        categoryGroup.getSelectionModel().selectedItemProperty().addListener(listener);
        levelGroup.getSelectionModel().selectedItemProperty().addListener(listener);

        multiplier.getItems().addAll(BuySellMode.values());
        multiplier.setConverter(new StringConverter<>() {
            @Override
            public String toString(BuySellMode object) {
                switch (object) {
                    case FullPrice:
                        return "Full Price";
                    case SellHalf:
                        return "Sell Half";
                    case Cashless:
                        return "Cashless";
                }
                return null;
            }

            @Override
            public BuySellMode fromString(String string) {
                switch (string) {
                    case "FullPrice":
                        return BuySellMode.FullPrice;
                    case "Sell Half":
                        return BuySellMode.SellHalf;
                    case "Cashless":
                        return BuySellMode.Cashless;
                }
                return null;
            }
        });
        multiplier.valueProperty().addListener(((observable, oldValue, newValue) -> {
            if(newValue != null) character.inventory().setMode(newValue);
        }));
        addMoney.setOnAction(event -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Add Money");
            dialog.setHeaderText("Add Money (in sp)");
            dialog.setContentText("Amount:");
            dialog.showAndWait().ifPresent(s ->
                    character.inventory().addMoney(Double.parseDouble(s)));
        });
    }

    protected void initializeShop() {
        categoryGroup = new CategoryAllItemsList(character, this::tryToBuy);
        levelGroup = new LevelAllItemsList(character, this::tryToBuy);
    }

    protected boolean tryToBuy(Item item, int count) {
        if(count != 2) return false;
        if(item instanceof BaseItem && item.hasExtension(BaseItemChoices.class)) {
            CustomItemController controller = new CustomItemController((BaseItem) item, i->tryToBuy(i, 2));
            Popup.popup("/fxml/equip/customItem.fxml", controller, Modality.NONE);
            return false;
        }
        if (!character.inventory().buy(item, 1)) {
            new Alert(Alert.AlertType.INFORMATION, "Not Enough Money!").showAndWait();
            return false;
        }else{
            return true;
        }
    }

    private void tryToSell(ItemCount item) {
        if (!character.inventory().sell(item.stats(), 1)) {
            new Alert(Alert.AlertType.INFORMATION, "Not Enough Items!").showAndWait();
        }
    }

    public void passValues(ObservableList<ItemCount> inventoryList) {
        inventory.setItems(new SortedList<>(inventoryList, Comparator.comparing(o -> o.stats().toString())));
        inventory.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selectedItem) -> {
            if(selectedItem != null) setDisplay(selectedItem.stats());
        });
        inventory.setOnMouseClicked((event) -> {
            if(event.getClickCount() % 2 == 0) {
                ItemCount item = inventory.getSelectionModel().getSelectedItem();
                tryToSell(item);
            }
        });
    }

    protected void setDisplay(Item selectedItem) {
        String s = EquipmentHTMLGenerator.parse(selectedItem);
        itemDisplay.getEngine().loadContent(s);
//        System.out.println(s);
    }
}
