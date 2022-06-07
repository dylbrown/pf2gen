package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import model.data_managers.sources.Source;
import ui.controls.Popup;
import ui.controls.lists.SourceList;
import ui.controls.lists.ThreeState;
import ui.controls.lists.entries.SourceEntry;

import java.util.*;

public class SourceSelectController implements Popup.Controller {
    private Stage stage = null;
    private final Collection<Source> preSelectedSources;
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

    public SourceSelectController() {
        this(Collections.emptyList());
    }

    public SourceSelectController(Collection<Source> preSelectedSources) {
        this.preSelectedSources = preSelectedSources;
    }

    @FXML
    private void initialize() {
        display.getEngine().setUserStyleSheetLocation(
                Objects.requireNonNull(getClass().getResource("/webview_style.css")).toString());
        sourceList = new SourceList((source, i) -> display.getEngine().loadContent(source.getDescription()));
        sourceList.selectAndLock(preSelectedSources);
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
        getSources(sources, sourceList.getRoot());
        return sources;
    }

    private void getSources(List<Source> sources, TreeItem<SourceEntry> root) {
        for (TreeItem<SourceEntry> treeItem : root.getChildren()) {
            if(treeItem.getValue().stateProperty().get() == ThreeState.True
                    && treeItem.getValue().getContents() != null) {
                sources.add(treeItem.getValue().getContents());
            }
            getSources(sources, treeItem);
        }
    }

    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
