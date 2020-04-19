package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import model.abilities.abilitySlots.Choice;
import model.abilities.abilitySlots.FeatSlot;
import model.abilities.abilitySlots.SingleChoiceList;
import model.abilities.abilitySlots.SingleChoiceSlot;
import model.player.ArbitraryChoice;
import ui.Main;
import ui.controls.FeatSelectionPane;
import ui.controls.SelectionPane;
import ui.controls.SingleSelectionPane;
import ui.controls.lists.DecisionsList;
import ui.controls.lists.entries.DecisionEntry;

public class DecisionsController {

    @FXML
    private ToggleGroup filterChoices;
    @FXML
    private WebView display;
    @FXML
    private BorderPane decisionsPaneContainer, choicesContainer;

    @FXML
    private void initialize() {
        DecisionsList decisionsList = new DecisionsList((choice, i) ->
                setChoices(choice), Main.character.decisions().getDecisions());
        decisionsPaneContainer.setCenter(decisionsList);
        decisionsList.getSelectionModel().selectedItemProperty().addListener((o, oldVal, newVal)->{
            if(newVal != null)
                setChoices(newVal.getValue());
        });


    }

    private void setChoices(DecisionEntry entry) {
        Choice choice = entry.getChoice();
        if(choice == null) return;
        if(choice instanceof FeatSlot) {
            choicesContainer.setCenter(new FeatSelectionPane((FeatSlot)choice, display, filterChoices));
        }else if(choice instanceof SingleChoiceSlot){
            choicesContainer.setCenter(new FeatSelectionPane((SingleChoiceSlot)choice, display, filterChoices));
        }else if(choice instanceof SingleChoiceList){
            choicesContainer.setCenter(new SingleSelectionPane<>((SingleChoiceList<?>) choice, display));
        }else if (choice instanceof ArbitraryChoice) {
            choicesContainer.setCenter(new SelectionPane<>((ArbitraryChoice) choice, display));
        }else{
            choicesContainer.setCenter(new AnchorPane());
        }
    }
}
