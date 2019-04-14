package ui.customControls;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class AncestryTab extends AnchorPane {
    public AncestryTab() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "../fxml/ancestryTab.fxml"));
        fxmlLoader.setRoot(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}
