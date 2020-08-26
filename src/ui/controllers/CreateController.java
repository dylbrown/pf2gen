package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import model.data_managers.sources.Source;
import ui.controls.lists.SourceList;
import ui.controls.lists.entries.SourceEntry;

import java.util.ArrayList;
import java.util.List;

public class CreateController {
    private final Stage stage;
    @FXML
    private BorderPane sources;
    @FXML
    private WebView display;
    @FXML
    private Button done;
    private SourceList sourceList;
    private boolean success = false;

    public boolean isSuccess() {
        return success;
    }

    public CreateController(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void initialize() {
        display.getEngine().setUserStyleSheetLocation(getClass().getResource("/webview_style.css").toString());
        sourceList = new SourceList((source, i) -> display.getEngine().loadContent(source.getDescription()));
        sourceList.getSelectionModel().selectedItemProperty().addListener((o, oldVal, newVal) -> {
            if(newVal.getValue() != null && newVal.getValue().getContents() != null) {
                display.getEngine().loadContent(newVal.getValue().getContents().getDescription());
            }
        });
        sources.setCenter(sourceList);
        done.setOnAction(e->{
            success = true;
            stage.close();
        });
    }

    public List<Source> getSources() {
        List<Source> sources = new ArrayList<>();
        for (TreeItem<SourceEntry> treeItem : sourceList.getRoot().getChildren()) {
            if(treeItem.getValue().isEnabled() && treeItem.getValue().getContents() != null) {
                sources.add(treeItem.getValue().getContents());
            }
        }
        return sources;
    }
}
