package ui.controllers;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import model.CharacterManager;
import model.ability_slots.Choice;
import model.ability_slots.ChoiceList;
import model.ability_slots.FeatSlot;
import model.ability_slots.SingleChoiceSlot;
import ui.controls.FeatSelectionPane;
import ui.controls.SelectionPane;
import ui.controls.lists.DecisionsList;
import ui.controls.lists.entries.DecisionEntry;
import ui.html.HTMLGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@SuppressWarnings("rawtypes")
public class DecisionsController {

    @FXML
    private ToggleGroup filterChoices;
    @FXML
    private WebView display;
    @FXML
    private BorderPane decisionsPaneContainer, choicesContainer;
    private final Map<Choice, Node> nodes = new HashMap<>();
    private DecisionsList decisionsList;

    @FXML
    private void initialize() {
        initialize(CharacterManager.getActive().decisions().getDecisions());
    }

    protected void initialize(ObservableList<Choice> decisions) {
        display.getEngine().setUserStyleSheetLocation(getClass().getResource("/webview_style.css").toString());
        decisionsList = new DecisionsList((treeItem, i) -> setChoices(treeItem),
                decisions);
        decisionsList.getSelectionModel().selectedItemProperty().addListener((o, oldVal, newVal)->setChoices(newVal));
        decisionsPaneContainer.setCenter(decisionsList);
    }

    private Choice<?> setChoices(TreeItem<DecisionEntry> treeItem) {
        if(treeItem == null) return null;
        DecisionEntry entry = treeItem.getValue();
        Choice<?> choice = entry.getChoice();
        if(choice == null) {
            choice = setChoices(treeItem.getParent());
            setDisplay(choice, entry.getChosenValue());
            return choice;
        }
        Node node = nodes.get(choice);
        if(node == null) {
            if(choice instanceof FeatSlot) {
                node = new FeatSelectionPane((FeatSlot)choice, display, filterChoices);
            }else if(choice instanceof SingleChoiceSlot){
                node = new FeatSelectionPane((SingleChoiceSlot)choice, display, filterChoices);
            }else if(choice instanceof ChoiceList){
                node = new SelectionPane<>((ChoiceList<?>) choice, display);
            }else {
                node = new AnchorPane();
            }
            nodes.put(choice, node);
        }
        choicesContainer.setCenter(node);

        return choice;
    }

    private <T> void setDisplay(Choice<T> choice, Object chosenValue) {
        Function<T, String> generator = HTMLGenerator.getGenerator(choice.getOptionsClass());
        if(chosenValue != null) {
            display.getEngine().loadContent(
                    generator.apply(choice.getOptionsClass().cast(chosenValue))
            );
        }
    }

    public void navigate(List<String> path) {
        for (TreeItem<DecisionEntry> choice : decisionsList.getRoot().getChildren()) {
            if(path.get(0).equalsIgnoreCase(choice.getValue().toString())) {
                decisionsList.getSelectionModel().select(choice);
                break;
            }
        }
    }
}
