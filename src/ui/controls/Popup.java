package ui.controls;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public abstract class Popup {
    public static void popup(String path, Popup.Controller controller) {
        popup(path, controller, Modality.APPLICATION_MODAL);
    }

    public interface Controller {
        void setStage(Stage stage);
    }
    public static void popup(String path, Popup.Controller controller, Modality modality){
        FXMLLoader loader = new FXMLLoader(controller.getClass().getResource(path));
        Stage popup = new Stage();
        controller.setStage(popup);
        popup.initModality(modality);
        loader.setController(controller);
        try {
            popup.setScene(new Scene(loader.load()));
            popup.getScene().getRoot().setStyle("-fx-base: rgba(45, 49, 50, 255);");
            popup.getScene().getStylesheets().add("/style.css");
            popup.showAndWait();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}
