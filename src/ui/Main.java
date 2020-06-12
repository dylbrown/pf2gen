package ui;

import javafx.application.Application;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import model.player.PC;

public class Main extends Application {
    public static final PC character;

    static{
        character = new PC();
    }

    private static final Property<Scene> scene = new SimpleObjectProperty<>();

    @Override
    public void start(Stage primaryStage) throws Exception{
        long startTime = System.currentTimeMillis();
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/home.fxml"));
        primaryStage.setTitle("PF2Gen");
        scene.setValue(new Scene(root, 900, 600));
        primaryStage.getIcons().add(new Image("/PF2Gen.png"));
        primaryStage.setScene(scene.getValue());
        root.setStyle("-fx-base: rgba(45, 49, 50, 255);");
        primaryStage.getScene().getStylesheets().add("/style.css");
        System.out.println(System.currentTimeMillis() - startTime + " ms");
        primaryStage.show();
        // primaryStage.setMaximized(true);
    }


    public static void main(String[] args) {
        launch(args);
    }

    public static void addEventFilter(EventType<KeyEvent> type, EventHandler<KeyEvent> handler){
        if(scene.getBean() != null)
            scene.getValue().addEventFilter(type, handler);
        else
            scene.addListener((change)-> addEventFilter(type, handler));
    }
}
