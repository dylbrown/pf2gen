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
import ui.controllers.Controller;

public class Main extends Application {

    private static final Property<Scene> scene = new SimpleObjectProperty<>();
    public static final Controller CONTROLLER = new Controller();

    public static Scene getScene() {
        return scene.getValue();
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        long startTime = System.currentTimeMillis();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
        fxmlLoader.setController(CONTROLLER);
        Parent root = fxmlLoader.load();
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
