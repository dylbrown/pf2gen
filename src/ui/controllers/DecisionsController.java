package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import model.ability_slots.Choice;
import model.ability_slots.ChoiceList;
import model.ability_slots.FeatSlot;
import model.ability_slots.SingleChoiceSlot;
import ui.Main;
import ui.controls.FeatSelectionPane;
import ui.controls.SelectionPane;
import ui.controls.lists.DecisionsList;
import ui.controls.lists.entries.DecisionEntry;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("rawtypes")
public class DecisionsController {

    @FXML
    private ToggleGroup filterChoices;
    @FXML
    private WebView display;
    @FXML
    private BorderPane decisionsPaneContainer, choicesContainer;
    private Map<Choice, Node> nodes = new HashMap<>();

    @FXML
    private void initialize() {
        DecisionsList decisionsList = new DecisionsList((treeItem, i) -> setChoices(treeItem),
                Main.character.decisions().getDecisions());
        decisionsPaneContainer.setCenter(decisionsList);
    }

    private void setChoices(TreeItem<DecisionEntry> treeItem) {
        if(treeItem == null) return;
        DecisionEntry entry = treeItem.getValue();
        Choice<?> choice = entry.getChoice();
        if(choice == null) {
            setChoices(treeItem.getParent());
            return;
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
    }
}
