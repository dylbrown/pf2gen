package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import model.FileLoader;
import model.abc.Background;

import static ui.Main.character;

public class BackgroundTabController {
    @FXML
    private ListView<Background> backgroundList;
    @FXML
    private Label backgroundDisplay;
    @FXML
    private Button setBackground;

    @FXML
    private void initialize() {
        for(Background background: FileLoader.getBackgrounds()) {
            backgroundList.getItems().add(background);
        }

        setBackground.setOnAction((event) -> {
            Background selectedItem = backgroundList.getSelectionModel().getSelectedItem();
            backgroundDisplay.setText(selectedItem.toString());
            character.setBackground(selectedItem);
        });
    }
}
