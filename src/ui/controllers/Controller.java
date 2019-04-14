package ui.controllers;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import model.abc.Class;
import model.abilityScores.AbilityScore;
import model.enums.Attribute;
import model.enums.Proficiency;

import java.util.Map;

import static ui.Main.character;

public class Controller {
    @FXML
    private ListView<String> classList;
    @FXML
    private TextField characterName;
    @FXML
    private Button setClass;
    @FXML
    private Label fortitudeDisplay;
    @FXML
    private Label reflexDisplay;
    @FXML
    private Label willDisplay;
    @FXML
    private Label classDisplay;
    @FXML
    private Button display;
    @FXML
    private Label proficienciesDisplay;
    @FXML
    private Label healthDisplay;
    @FXML
    private Label Str;
    @FXML
    private Label Dex;
    @FXML
    private Label Con;
    @FXML
    private Label Int;
    @FXML
    private Label Wis;
    @FXML
    private Label Cha;
    @FXML
    private void initialize(){




        classList.setItems(FXCollections.observableArrayList (Class.getClassNames()));

        characterName.textProperty().addListener((observable, oldValue, newValue) -> character.setName(newValue));

        setClass.setOnAction((event) -> {
            String selectedItem = classList.getSelectionModel().getSelectedItem();
            classDisplay.setText(selectedItem);
            character.setClass(selectedItem);
        });

        display.setOnAction((event) -> {
            fortitudeDisplay.setText((character.getProficiency(Attribute.Fortitude).getValue() != null) ? character.getProficiency(Attribute.Fortitude).getValue().toString(): "Untrained");
            reflexDisplay.setText((character.getProficiency(Attribute.Reflex).getValue() != null) ? character.getProficiency(Attribute.Reflex).getValue().toString(): "Untrained");
            willDisplay.setText((character.getProficiency(Attribute.Will).getValue() != null) ? character.getProficiency(Attribute.Will).getValue().toString(): "Untrained");
            StringBuilder profs = new StringBuilder();
            for(Map.Entry<Attribute, ObservableValue<Proficiency>> entry: character.getProficiencies().entrySet()) {
                if(entry.getKey() == Attribute.Fortitude || entry.getKey() == Attribute.Reflex || entry.getKey() == Attribute.Will || entry.getValue() == null || entry.getValue().getValue() == null)
                    continue;
                profs.append(entry.getKey().name()).append(": ").append(entry.getValue().getValue().name()).append("\n");
            }
            proficienciesDisplay.setText(profs.toString());
            healthDisplay.setText(String.valueOf(character.getHP()));
            Str.setText(String.valueOf(character.getAbilityScore(AbilityScore.Str)));
            Dex.setText(String.valueOf(character.getAbilityScore(AbilityScore.Dex)));
            Con.setText(String.valueOf(character.getAbilityScore(AbilityScore.Con)));
            Int.setText(String.valueOf(character.getAbilityScore(AbilityScore.Int)));
            Wis.setText(String.valueOf(character.getAbilityScore(AbilityScore.Wis)));
            Cha.setText(String.valueOf(character.getAbilityScore(AbilityScore.Cha)));
        });
    }

}
