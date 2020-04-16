package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import model.abc.Background;
import model.xml_parsers.abc.BackgroundsLoader;

import static ui.Main.character;

public class BackgroundTabController {
    @FXML
    private ListView<Background> backgroundList;
    @FXML
    private Label backgroundDisplay;
    @FXML
    private Label backgroundDesc;
    @FXML
    private Label skill1;

    @FXML
    private Label skill2;
    @FXML
    private Label feat;
    @FXML
    private Label mods;

    @FXML
    private void initialize() {
        try{
            backgroundList.getItems().addAll(BackgroundsLoader.instance().parse());
        }catch (Exception e){
            e.printStackTrace();
        }

        backgroundList.setOnMouseClicked((event) -> {
            if(event.getClickCount() == 2) {
                Background selectedItem = backgroundList.getSelectionModel().getSelectedItem();
                character.setBackground(selectedItem);
            }
        });
        character.getBackgroundProperty().addListener((o, oldVal, newVal)-> backgroundDisplay.setText(newVal.getName()));
        backgroundList.getSelectionModel().selectedItemProperty().addListener((event)->{
            Background item = backgroundList.getSelectionModel().getSelectedItem();
            backgroundDesc.setText(item.getDesc());
            skill1.setText(item.getMods().get(0).toNiceAttributeString());
            skill2.setText(item.getMods().get(1).toNiceAttributeString());
            mods.setText(item.getModString());
            feat.setText(item.getFreeFeat().getCurrentAbility().toString());
        });
    }
}
