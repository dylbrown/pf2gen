package ui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.util.StringConverter;
import model.CharacterManager;
import model.data_managers.sources.Source;
import model.player.PC;
import model.player.SourcesManager;
import ui.Main;
import ui.controls.Popup;
import ui.controls.SaveLoadController;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Controller {
    @FXML
    private BorderPane main;
    @FXML
    private ComboBox<PC> characterSelect;
    private final Map<PC, Node> characterPanes = new HashMap<>();
    @FXML
    private MenuItem new_menu, open_menu, close_menu, save_menu, saveAs_menu, addSources_menu,
            statblock_menu, printableSheet_menu, indexCard_menu, jquerySheet_menu, about_menu, gm_menu;
    private final Map<PC, CharacterController> controllers = new HashMap<>();

    @FXML
    private void initialize(){
        characterSelect.setItems(CharacterManager.getCharacters());
        characterSelect.setConverter(new StringConverter<>() {
            @Override
            public String toString(PC pc) {
                if (pc == null) return "";
                return pc.qualities().get("name");
            }

            @Override
            public PC fromString(String s) {
                System.out.println("Unexpected From String Call");
                return null;
            }
        });
        CharacterManager.activeProperty().addListener((o, oldVal, newVal)->
                characterSelect.getSelectionModel().select(newVal));
        characterSelect.getSelectionModel().selectedItemProperty().addListener((o, oldVal, newVal)->{
            if(newVal != null) {
                CharacterManager.setActive(newVal);
                main.setCenter(characterPanes.computeIfAbsent(newVal, pc->{
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/character.fxml"));
                    CharacterController controller = new CharacterController(pc);
                    controllers.put(pc, controller);
                    loader.setController(controller);
                    try {
                        return loader.load();
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }));
            } else main.setCenter(new BorderPane());
        });

        new_menu.setOnAction(e -> {
            SourceSelectController controller = new SourceSelectController();
            Popup.popup("/fxml/sourceSelect.fxml", controller);
            if(controller.isSuccess()) {
                PC pc = new PC(new SourcesManager(controller.getSources()));
                CharacterManager.add(pc);
                characterSelect.getSelectionModel().select(pc);
            }
        });
        open_menu.setOnAction(e -> {
            PC pc = new PC(new SourcesManager());
            SaveLoadController.getInstance().load(pc, main.getScene());
            CharacterManager.add(pc);
            characterSelect.getSelectionModel().select(pc);
        });
        close_menu.setOnAction(e -> {
            PC active = CharacterManager.getActive();
            if (characterSelect.getItems().size() > 1) {
                int selectedIndex = characterSelect.getSelectionModel().getSelectedIndex();
                PC newActive = characterSelect.getItems().get((selectedIndex + 1) % characterSelect.getItems().size());
                CharacterManager.setActive(newActive);
                characterSelect.getSelectionModel().select(newActive);
            }
            CharacterManager.remove(active);
        });
        save_menu.setOnAction(e -> SaveLoadController.getInstance().save(CharacterManager.getActive(), main.getScene()));
        saveAs_menu.setOnAction(e -> SaveLoadController.getInstance().saveAs(CharacterManager.getActive(), main.getScene()));
        addSources_menu.setOnAction(e -> {
            SourceSelectController controller = new SourceSelectController(CharacterManager.getActive().sources().getSources());
            Popup.popup("/fxml/sourceSelect.fxml", controller);
            if(controller.isSuccess()) {
                for (Source source : controller.getSources()) {
                    CharacterManager.getActive().sources().add(source);
                    CharacterManager.reload(CharacterManager.getActive());
                }
            }
        });
        statblock_menu.setOnAction(e ->
                SaveLoadController.getInstance().export("statblock.ftl", main.getScene()));
        printableSheet_menu.setOnAction(e ->
                SaveLoadController.getInstance().export("printableSheet.html.ftl", main.getScene()));
        indexCard_menu.setOnAction(e ->
                SaveLoadController.getInstance().export("index_card.html.ftl", main.getScene()));
        jquerySheet_menu.setOnAction(e ->
                SaveLoadController.getInstance().export("csheet_jquery.html.ftl", main.getScene()));
        about_menu.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("About");
            final Properties properties = new Properties();
            try {
                properties.load(this.getClass().getResourceAsStream("/project.properties"));
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            String version = properties.getProperty("version");
            if(version == null || version.startsWith("${"))
                version = "-NOT COMPILED WITH MAVEN-";
            else
                version = "v" + version;
            alert.setHeaderText("PF2Gen "+version);
            alert.setContentText("Created by Dylan Brown.\nThis character creator uses trademarks and/or copyrights owned by Paizo Inc., which are used under Paizo's Community Use Policy. We are expressly prohibited from charging you to use or access this content. This character creator is not published, endorsed, or specifically approved by Paizo Inc. For more information about Paizo's Community Use Policy, please visit paizo.com/communityuse. For more information about Paizo Inc. and Paizo products, please visit paizo.com.");
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alert.show();
        });
        gm_menu.setOnAction(e->{
            try {
                long startTime = System.currentTimeMillis();
                Parent root = FXMLLoader.load(getClass().getResource("/fxml/gm/gm.fxml"));
                Scene scene = Main.getScene();
                scene.setRoot(root);
                root.setStyle("-fx-base: rgba(45, 49, 50, 255);");
                System.out.println(System.currentTimeMillis() - startTime + " ms");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });
    }

    public void navigate(List<String> path) {
        controllers.get(CharacterManager.getActive()).navigate(path);
    }
}
