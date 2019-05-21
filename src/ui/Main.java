package ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import model.PC;

public class Main extends Application {
    public static final PC character;

    static{
        character = new PC();
    }
    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/ui/fxml/home.fxml"));
        primaryStage.setTitle("PF2Gen");
        primaryStage.getIcons().add(new Image("PF2Gen.png"));
        primaryStage.setScene(new Scene(root, 800, 450));
        root.setStyle("-fx-base: rgba(45, 49, 50, 255);");
        primaryStage.getScene().getStylesheets().add("style.css");
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
