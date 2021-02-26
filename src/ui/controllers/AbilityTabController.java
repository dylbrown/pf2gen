package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import model.CharacterManager;
import model.ability_scores.AbilityModChoice;
import model.ability_scores.AbilityScore;
import model.player.PC;
import ui.controls.AbilityEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AbilityTabController {
    @FXML
    FlowPane abilitiesList;

    private final List<AbilityModChoice> choices = new ArrayList<>();
    private final Map<AbilityModChoice, AbilityEntry> tracker = new HashMap<>();
    private PC character;

    @FXML
    private void initialize() {
        character = CharacterManager.getActive();
        updateTable();
        character.scores().addAbilityListener((observable)->updateTable());
        abilitiesList.prefWrapLengthProperty().bind(abilitiesList.widthProperty());
    }

    private void updateTable() {
        List<AbilityModChoice> removals = new ArrayList<>();
        for (AbilityModChoice abilityModChoice : tracker.keySet()) {
            if(!character.scores().getAbilityScoreChoices().contains(abilityModChoice)) {
                removals.add(abilityModChoice);
            }
        }
        for (AbilityModChoice removal : removals) {
            abilitiesList.getChildren().remove(tracker.remove(removal));
            choices.remove(removal);
        }
        for (AbilityModChoice choice : character.scores().getAbilityScoreChoices()) {
            boolean contains = false;
            for (AbilityModChoice abilityModChoice : choices) {
                if (abilityModChoice == choice) {
                    contains = true;
                    break;
                }
            }
            if(!contains){
                choices.add(choice);
                Label label = new Label("Type: "+choice.getType().name());
                ComboBox<AbilityScore> dropdown = new ComboBox<>();
                dropdown.getItems().addAll(choice.getChoices());
                dropdown.getSelectionModel().select(choice.getTarget());
                dropdown.setOnAction((event -> character.scores().choose(choice, dropdown.getValue())));
                AbilityEntry entry = new AbilityEntry(label, dropdown);
                abilitiesList.getChildren().add(entry);
                tracker.put(choice, entry);

                choice.getTargetProperty().addListener((o, oldVal, newVal)-> dropdown.getSelectionModel().select(newVal));
            }
        }
    }
}
