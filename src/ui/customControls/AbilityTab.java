package ui.customControls;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.GridPane;

import java.io.IOException;

public class AbilityTab extends GridPane {
    public AbilityTab(){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "../fxml/abilityTab.fxml"));
        fxmlLoader.setRoot(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}
