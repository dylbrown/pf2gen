package ui.customControls;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;

import java.io.IOException;

public class BackgroundTab extends Tab {
    public BackgroundTab() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "../fxml/backgroundTab.fxml"));
        fxmlLoader.setRoot(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}
