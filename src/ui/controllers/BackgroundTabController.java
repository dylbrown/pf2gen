package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import model.abc.Background;
import model.xml_parsers.BackgroundsLoader;

import static ui.Main.character;

public class BackgroundTabController {
    @FXML
    private ListView<Background> backgroundList;
    @FXML
    private Label backgroundDisplay;
    @FXML
    private Label backgroundDesc;
    @FXML
    private Label skill;
    @FXML
    private Label feat;
    @FXML
    private Label mods;

    @FXML
    private void initialize() {
        for(Background background: FileLoader.getBackgrounds()) {
            backgroundList.getItems().add(background);
        }

        backgroundList.setOnMouseClicked((event) -> {
            if(event.getClickCount() == 2) {
                Background selectedItem = backgroundList.getSelectionModel().getSelectedItem();
                backgroundDisplay.setText(selectedItem.toString());
                character.setBackground(selectedItem);
            }
        });
        backgroundList.getSelectionModel().selectedItemProperty().addListener((event)->{
            backgroundDesc.setText(backgroundList.getSelectionModel().getSelectedItem().getDesc());
            skill.setText(backgroundList.getSelectionModel().getSelectedItem().getMod().toNiceAttributeString());
            mods.setText(backgroundList.getSelectionModel().getSelectedItem().getModString());
        });
    }
}
