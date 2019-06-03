package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import model.abilities.Ability;
import model.abilities.Activity;
import model.enums.Action;
import ui.TemplateFiller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Objects;

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
    @FXML
    private Label level;
    @FXML
    private Button levelUp;
    private String htmlContent;


    @FXML
    private void initialize(){

        export.setOnAction((event -> {
            FileChooser fileChooser = new FileChooser();
            if(!Objects.equals(characterName.getText(), ""))
                fileChooser.setInitialFileName(characterName.getText().replaceAll(" ",""));
            fileChooser.setInitialDirectory(new File("./"));
            //Set extension filter for text files
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("HTML files", "*.html")
            );

            //Show save file dialog
            File file = fileChooser.showSaveDialog(export.getScene().getWindow());

            if (file != null) {
                try {
                    PrintWriter out = new PrintWriter(file);
                    out.println(htmlContent);
                    out.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }));




        classList.getItems().addAll(FileLoader.getClasses());

        characterName.textProperty().addListener((observable, oldValue, newValue) -> character.setName(newValue));
        displayTab.setOnSelectionChanged((event) -> {
            if(displayTab.isSelected()) {
                htmlContent = TemplateFiller.getStatBlock();
                display.getEngine().loadContent(htmlContent);
            }
        });
        level.setText("0");
        character.getLevelProperty().addListener((event)-> level.setText(character.getLevelProperty().get().toString()));
        levelUp.setOnAction((event -> character.levelUp()));
    }

    private String getAbility(Ability ability) {
        StringBuilder abilityBuilder = new StringBuilder();
        abilityBuilder.append("<b>");
        switch (((Activity) ability).getCost()) {
            case Free:
                abilityBuilder.append("Ⓕ ");
                break;
            case Reaction:
                abilityBuilder.append("Ⓡ ");
                break;
            case One:
                abilityBuilder.append("① ");
                break;
            case Two:
                abilityBuilder.append("② ");
                break;
            case Three:
                abilityBuilder.append("③ ");
                break;
        }
        abilityBuilder.append(ability.toString()).append("</b> ").append(ability.getDesc());
        if(((Activity) ability).getCost() == Action.Reaction)
            abilityBuilder.append(" <b>Trigger</b> ").append(((Activity) ability).getTrigger());
        return abilityBuilder.toString();
    }
}
