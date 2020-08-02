package ui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.web.WebView;
import ui.Main;
import ui.controls.SaveLoadController;
import ui.ftl.TemplateFiller;

import java.io.File;
import java.io.IOException;

import static ui.Main.character;

public class Controller {
    @FXML
    private Tab tab_abilityScores, tab_skills, tab_decisions, tab_equipment, tab_spells;
    @FXML
    private MenuItem new_menu, open_menu, save_menu, saveAs_menu,
            statblock_menu, printableSheet_menu, indexCard_menu, jquerySheet_menu, about_menu, gm_menu;
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
                SaveLoadController.getInstance().export("sheets/statblock.ftl", display.getScene()));
        printableSheet_menu.setOnAction(e ->
                SaveLoadController.getInstance().export("sheets/printableSheet.html.ftl", display.getScene()));
        indexCard_menu.setOnAction(e ->
                SaveLoadController.getInstance().export("sheets/index_card.html.ftl", display.getScene()));
        jquerySheet_menu.setOnAction(e ->
                SaveLoadController.getInstance().export("sheets/csheet_jquery.html.ftl", display.getScene()));
        about_menu.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("About");
            alert.setHeaderText("PF2Gen v"+getClass().getPackage().getImplementationVersion());
            alert.setContentText("Created by Dylan Brown.");
            alert.show();
        });
        gm_menu.setOnAction(e->{
            try {
                long startTime = System.currentTimeMillis();
                Parent root = FXMLLoader.load(getClass().getResource("/fxml/gm/gm.fxml"));
                Scene scene = Main.getScene();
                scene.setRoot(root);
                root.setStyle("-fx-base: rgba(45, 49, 50, 255);");
                System.out.println(System.currentTimeMillis() - startTime + " ms");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });
    }
}
