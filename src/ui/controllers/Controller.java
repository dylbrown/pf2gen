package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import ui.Main;
import ui.ftl.TemplateFiller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Objects;

import static ui.Main.character;

public class Controller {
    @FXML
    private Button export;
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
        export.setOnAction((event -> {
            FileChooser fileChooser = new FileChooser();
            if(!Objects.equals(character.getName(), ""))
                fileChooser.setInitialFileName(character.getName().replaceAll("[ ?!]",""));
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
                    out.println(TemplateFiller.getSheet());
                    out.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }));
        displayTab.setOnSelectionChanged((event) -> {
            if(displayTab.isSelected()) {
                htmlContent = TemplateFiller.getStatBlock();
                htmlContent = htmlContent.replace("</title>", "</title>\n<base href=\"file:///"
                        + new File("jquery/").getAbsolutePath().replaceAll("\\\\", "/")
                        + "/\"/>");
                display.getEngine().loadContent(htmlContent);
            }
        });
    }
}
