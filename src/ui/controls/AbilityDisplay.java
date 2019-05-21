package ui.controls;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.GridPane;

import java.io.IOException;

public class AbilityTab extends GridPane {
    public AbilityTab(){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "/ui/fxml/abilityDisplay.fxml"));
        fxmlLoader.setRoot(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}
