package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import model.FileLoader;
import model.abc.Ancestry;
import ui.Main;

public class AncestryTabController {
    @FXML
    private ListView<Ancestry> ancestryList;
    @FXML
    private Button setAncestry;
    @FXML
    private Label ancestryDisplay;
    @FXML
    private void initialize() {
        ancestryList.getItems().addAll(FileLoader.getAncestries());

        setAncestry.setOnAction((event) -> {
            Ancestry selectedItem = ancestryList.getSelectionModel().getSelectedItem();
            ancestryDisplay.setText(selectedItem.toString());
            Main.character.setAncestry(selectedItem);
        });
    }
}
