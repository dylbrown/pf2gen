package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.web.WebView;
import ui.Main;
import ui.controls.SaveLoadController;
import ui.ftl.TemplateFiller;

import java.io.File;

import static ui.Main.character;

public class Controller {
    @FXML
    private Tab tab_abilityScores, tab_skills, tab_decisions, tab_equipment, tab_spells;
    @FXML
    private MenuItem new_menu, open_menu, save_menu, saveAs_menu,
            statblock_menu, printableSheet_menu, jquerySheet_menu, about_menu;
    @FXML
    private Tab displayTab;
    @FXML
    private WebView display;
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
        displayTab.setOnSelectionChanged((event) -> {
            if(displayTab.isSelected()) {
                htmlContent = TemplateFiller.getStatBlock();
                htmlContent = htmlContent.replace("</title>", "</title>\n<base href=\"file:///"
                        + new File("jquery/").getAbsolutePath().replaceAll("\\\\", "/")
                        + "/\"/>");
                display.getEngine().loadContent(htmlContent);
            }
        });

        new_menu.setOnAction(e -> SaveLoadController.getInstance().reset());
        open_menu.setOnAction(e -> SaveLoadController.getInstance().load(display.getScene()));
        save_menu.setOnAction(e -> SaveLoadController.getInstance().save(character.qualities().get("name"), display.getScene()));
        saveAs_menu.setOnAction(e -> SaveLoadController.getInstance().saveAs(character.qualities().get("name"), display.getScene()));
        statblock_menu.setOnAction(e ->
                SaveLoadController.getInstance().export("statblock.ftl", display.getScene()));
        printableSheet_menu.setOnAction(e ->
                SaveLoadController.getInstance().export("printableSheet.html.ftl", display.getScene()));
        jquerySheet_menu.setOnAction(e ->
                SaveLoadController.getInstance().export("csheet_jquery.html.ftl", display.getScene()));
        about_menu.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("About");
            alert.setHeaderText("PF2Gen v0.0.0-alpha");
            alert.setContentText("Created by Dylan Brown.");
            alert.show();
        });
    }
}
