package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.web.WebView;
import model.CharacterManager;
import model.player.PC;
import ui.Main;
import ui.ftl.TemplateFiller;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CharacterController {
    private final PC pc;
    private final TemplateFiller templateFiller;
    @FXML
    private Tab tab_abilityScores, tab_skills, tab_decisions, tab_equipment, tab_spells;
    @FXML
    private Tab displayTab;
    @FXML
    private WebView display;
    private String htmlContent;

    @FXML
    private TabPane rootTabs;

    public CharacterController(PC pc) {
        this.pc = pc;
        this.templateFiller = new TemplateFiller(pc);
        fillers.put(pc, templateFiller);
    }

    private static final Map<PC, TemplateFiller> fillers = new HashMap<>();
    public static TemplateFiller getFiller(PC pc) {
        return fillers.get(pc);
    }

    @FXML
    private void initialize() {
        Main.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.TAB && event.isControlDown() && CharacterManager.getActive() == pc) {
                rootTabs.fireEvent(event);
            }
        });
        displayTab.setOnSelectionChanged((event) -> {
            if(displayTab.isSelected()) {
                htmlContent = templateFiller.getStatBlock();
                htmlContent = htmlContent.replace("</title>", "</title>\n<base href=\"file:///"
                        + new File("jquery/").getAbsolutePath().replaceAll("\\\\", "/")
                        + "/\"/>");
                display.getEngine().loadContent(htmlContent);
            }
        });
    }
}
