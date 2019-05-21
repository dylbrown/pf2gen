package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import model.ability_scores.AbilityModChoice;
import model.ability_scores.AbilityScore;
import ui.Main;

import java.util.*;

public class AbilityTabController {
    @FXML
    private GridPane gridPane;
    @FXML
    private void initialize() {
        for(Node node: gridPane.getChildren()) {
            int row = GridPane.getRowIndex(node);
            int col = GridPane.getColumnIndex(node);
            switch(row) {

            }
        }
    }
}
