package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import model.data_managers.SaveLoadManager;
import ui.Main;
import ui.TemplateFiller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Objects;

import static ui.Main.character;

public class Controller {
    @FXML
    private Button save;
    @FXML
    private Button load;
    @FXML
    private Button export;
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
    @FXML
    private Button levelDown;
    private String htmlContent;

    @FXML
    private TabPane rootTabs;


    @FXML
    private void initialize(){
        Main.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.TAB && event.isControlDown()) {
                rootTabs.fireEvent(event);
            }
        });
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
        levelDown.setOnAction((event -> character.levelDown()));

        save.setOnAction((event -> save()));
        load.setOnAction((event -> load()));
    }

    private void load() {
        FileChooser fileChooser = new FileChooser();
        if(!Objects.equals(characterName.getText(), ""))
            fileChooser.setInitialFileName(characterName.getText().replaceAll(" ",""));
        fileChooser.setInitialDirectory(new File("./"));
        //Set extension filter for text files
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PF2 files", "*.pf2")
        );

        //Show save file dialog
        File file = fileChooser.showOpenDialog(export.getScene().getWindow());
        SaveLoadManager.load(file);

        characterName.setText(character.getName());
        level.setText(String.valueOf(character.getLevel()));
    }

    private void save() {
        FileChooser fileChooser = new FileChooser();
        if(!Objects.equals(characterName.getText(), ""))
            fileChooser.setInitialFileName(characterName.getText().replaceAll(" ",""));
        fileChooser.setInitialDirectory(new File("./"));
        //Set extension filter for text files
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PF2 files", "*.pf2")
        );

        //Show save file dialog
        File file = fileChooser.showSaveDialog(export.getScene().getWindow());

        SaveLoadManager.save(file);
    }
}
