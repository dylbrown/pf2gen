package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.web.WebView;
import model.abilityScores.AbilityScore;
import model.enums.Attribute;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static ui.Main.character;

public class Controller {
    @FXML
    private ListView<Class> classList;
    @FXML
    private TextField characterName;
    @FXML
    private Tab displayTab;
    @FXML
    private WebView display;

    private static final String htmlTemplate;
    @FXML
    private void initialize(){




        classList.getItems().addAll(FileLoader.getClasses());

        characterName.textProperty().addListener((observable, oldValue, newValue) -> character.setName(newValue));
        displayTab.setOnSelectionChanged((event) -> {
            if(displayTab.isSelected()) {
                display.getEngine().loadContent(
                        String.format(htmlTemplate, character.getName(), character.getLevel(),
                        addSign(character.getTotalMod(Attribute.Perception)),
                        addSign(character.getAbilityMod(AbilityScore.Str)),
                        addSign(character.getAbilityMod(AbilityScore.Dex)),
                        addSign(character.getAbilityMod(AbilityScore.Con)),
                        addSign(character.getAbilityMod(AbilityScore.Int)),
                        addSign(character.getAbilityMod(AbilityScore.Wis)),
                        addSign(character.getAbilityMod(AbilityScore.Cha)),
                        character.getAC(), character.getTAC(),
                        addSign(character.getTotalMod(Attribute.Fortitude)),
                        addSign(character.getTotalMod(Attribute.Reflex)),
                        addSign(character.getTotalMod(Attribute.Will)),
                        character.getHP(),
                        character.getSpeed()
                ));
            }
        });
    }

    static{
        String diskData;
        try {
            diskData = new String(Files.readAllBytes(new File("data/output.html").toPath()));
        } catch (IOException e) {
            e.printStackTrace();
            diskData = "";
        }
        htmlTemplate = diskData;
    }

    private String addSign(int mod) {
        return ((mod > 0) ? "+" : "")+ mod;
    }
}
