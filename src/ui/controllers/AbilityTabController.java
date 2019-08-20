package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import model.ability_scores.AbilityModChoice;
import model.ability_scores.AbilityScore;
import ui.Main;

import java.util.*;

public class AbilityTabController {
    @FXML
    GridPane abilitiesGrid;

    private final List<AbilityModChoice> choices = new ArrayList<>();
    private final Map<AbilityModChoice, List<Node>> tracker = new HashMap<>();

    @FXML
    private void initialize() {
        updateTable();
        Main.character.scores().addAbilityObserver((observable, arg)->updateTable());
    }

    private void updateTable() {
        List<AbilityModChoice> removals = new ArrayList<>();
        for (AbilityModChoice abilityModChoice : tracker.keySet()) {
            if(!Main.character.scores().getAbilityScoreChoices().contains(abilityModChoice)) {
                removals.add(abilityModChoice);
            }
        }
        for (AbilityModChoice removal : removals) {
            List<Node> removed = tracker.remove(removal);
            Integer rowIndex = GridPane.getRowIndex(removed.get(0));
            abilitiesGrid.getChildren().removeAll(removed);
            for (Node child : abilitiesGrid.getChildren()) {
                Integer otherIndex = GridPane.getRowIndex(child);
                if(otherIndex != null && otherIndex > rowIndex)
                    GridPane.setRowIndex(child, otherIndex - 1);
            }

            choices.remove(removal);
        }
        for (AbilityModChoice choice : Main.character.scores().getAbilityScoreChoices()) {
            boolean contains = false;
            for (AbilityModChoice abilityModChoice : choices) {
                if (abilityModChoice == choice) {
                    contains = true;
                    break;
                }
            }
            if(!contains){
                choices.add(choice);
                Label label;
                ComboBox<AbilityScore> dropdown = new ComboBox<>();
                dropdown.getItems().addAll(choice.getChoices());
                dropdown.setOnAction((event -> Main.character.scores().choose(choice, dropdown.getValue())));
                if(choice.getChoices().size() == 6)
                    label = new Label("Type: "+choice.getType().name()+" (Free Boost)");
                else
                    label = new Label("Type: "+choice.getType().name()+" (Boost Choice)");
                abilitiesGrid.addRow(choices.size(), label, dropdown);
                tracker.put(choice, Arrays.asList(label, dropdown));

                choice.getTargetProperty().addListener((o, oldVal, newVal)-> dropdown.getSelectionModel().select(newVal));
            }
        }
    }
}
