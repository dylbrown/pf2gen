package ui.controls;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.GridPane;

import java.io.IOException;

class AbilityDisplay extends GridPane {
    public AbilityDisplay(){
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