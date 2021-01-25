package ui.controllers.equip;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.layout.BorderPane;
import model.CharacterManager;
import model.items.Item;
import model.items.ItemFormula;
import model.player.PC;
import ui.controls.equipment.CategoryAllItemsList;
import ui.controls.equipment.LevelAllItemsList;
import ui.controls.lists.ObservableEntryList;
import ui.controls.lists.entries.ItemEntry;
import ui.controls.lists.factories.TreeCellFactory;
import ui.todo.FormulasKnownTracker;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FormulaTabController extends PurchaseTabController {
    @FXML
    private BorderPane boughtFormulasContainer, grantedFormulasContainer;
    @FXML
    private Label grantedFormulasRemaining;
    private ObservableEntryList<Item, ItemEntry> boughtFormulas, grantedFormulas;
    PC character;
    @FXML
    @Override
    protected void initialize() {
        character = CharacterManager.getActive();

        boughtFormulas = ObservableEntryList.makeList(
                character.inventory().getFormulasBought(),
                this::handleClick,
                item-> "Level " + item.getLevel(),
                ItemEntry::new,
                ItemEntry::new,
                this::makeColumns);
        boughtFormulasContainer.setCenter(boughtFormulas);
        grantedFormulas = ObservableEntryList.makeList(
                character.inventory().getFormulasGranted(),
                this::handleClick,
                item-> "Level " + item.getLevel(),
                ItemEntry::new,
                ItemEntry::new,
                this::makeColumns);
        grantedFormulasContainer.setCenter(grantedFormulas);

        ObservableMap<Integer, Integer> remainingTracker = FormulasKnownTracker.getFormulasRemainingTracker(character);
        refreshRemainingFormulas(remainingTracker);
        remainingTracker.addListener(
                (MapChangeListener<Integer, Integer>) change -> refreshRemainingFormulas(remainingTracker));

        super.initialize();
    }

    private void refreshRemainingFormulas(ObservableMap<Integer, Integer> formulasRemaining) {
        String remainingString = formulasRemaining.entrySet().stream().filter(e -> e.getValue() > 0).map(e -> e.getValue() + " × Level " + e.getKey()).collect(Collectors.joining(", "));
        if(remainingString.isBlank())
            remainingString = "None";
        grantedFormulasRemaining.setText("Formulas Remaining: " + remainingString);
    }

    @Override
    protected boolean tryToBuy(Item item, int count) {
        if(count != 2) return false;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Formula Add Choice");
        alert.setHeaderText("Buy or add from granted formulas.");
        alert.setContentText("");
        ButtonType buy = new ButtonType("Buy");
        ButtonType grant = new ButtonType("Use Granted");
        alert.getButtonTypes().setAll(buy, grant);
        Optional<ButtonType> result = alert.showAndWait();
        if(result.isEmpty())
            return false;
        if(result.get() == buy) {
            if (!character.inventory().addFormula(item, true)) {
                new Alert(Alert.AlertType.INFORMATION, "Not Enough Money!").showAndWait();
                return false;
            } else {
                return true;
            }
        } else if(result.get() == grant) {
            if (!character.inventory().addFormula(item, false)) {
                new Alert(Alert.AlertType.INFORMATION, "No granted formulas remaining!").showAndWait();
                return false;
            } else {
                return true;
            }
        }
        return false;
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
