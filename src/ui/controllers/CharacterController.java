package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import model.CharacterManager;
import model.player.PC;
import ui.Main;
import ui.ftl.TemplateFiller;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CharacterController {
    private final PC pc;
    private final TemplateFiller templateFiller;
    @FXML
    private Tab tab_abilityScores, tab_skills, tab_decisions, tab_equipment, tab_spells, tab_preview;
    @FXML
    private WebView display;
    @FXML
    private AnchorPane window_startingChoices, window_decisions, window_spells;
    @FXML
    private StartingTabController window_startingChoicesController;
    @FXML
    private DecisionsController window_decisionsController;
    @FXML
    private SpellsTabController window_spellsController;
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
        tab_preview.setOnSelectionChanged((event) -> {
            if(tab_preview.isSelected()) {
                htmlContent = templateFiller.getStatBlock();
                htmlContent = htmlContent.replace("</title>", "</title>\n<base href=\"file:///"
                        + new File("jquery/").getAbsolutePath().replaceAll("\\\\", "/")
                        + "/\"/>");
                display.getEngine().loadContent(htmlContent);
            }
        });
    }

    public void navigate(List<String> path) {
        for (Tab tab : rootTabs.getTabs()) {
            if(tab.getId().equalsIgnoreCase(path.get(0))) {
                path.remove(0);
                rootTabs.getSelectionModel().select(tab);
                if(path.size() > 0) {
                    switch (tab.getContent().getId()) {
                        case "window_startingChoices":
                            window_startingChoicesController.navigate(path);
                            break;
                        case "window_decisions":
                            window_decisionsController.navigate(path);
                            break;
                        case "window_spells":
                            window_spellsController.navigate(path);
                            break;
                    }
                }
            }
        }
    }
}
