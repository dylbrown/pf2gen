package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;

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
