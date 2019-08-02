package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import model.abc.PClass;
import model.xml_parsers.ClassesLoader;

import static ui.Main.character;

public class ClassTabController {
    @FXML
    private ListView<PClass> classList;
    @FXML
    private Label classDisplay;
    @FXML
    private Label classDesc;

    @FXML
    private void initialize() {
        try{
        classList.getItems().addAll(new ClassesLoader().parse());
        }catch (Exception e){
            e.printStackTrace();
        }

        classList.setOnMouseClicked((event) -> {
            if(event.getClickCount() == 2) {
                PClass selectedItem = classList.getSelectionModel().getSelectedItem();
                character.setClass(selectedItem);
            }
        });
        character.getPClassProperty().addListener((o, oldVal, newVal)->classDisplay.setText(newVal.getName()));
        classList.getSelectionModel().selectedItemProperty().addListener((event)-> classDesc.setText(classList.getSelectionModel().getSelectedItem().getDesc()));
    }
}
