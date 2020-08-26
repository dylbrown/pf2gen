package ui.controls;

import javafx.scene.Scene;
import javafx.stage.FileChooser;
import model.CharacterManager;
import model.data_managers.SaveLoadManager;
import model.player.PC;
import ui.controllers.CharacterController;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Objects;

public class SaveLoadController {
    private File saveLocation = null;
    private File exportLocation = null;

    private SaveLoadController() {

    }

    private static SaveLoadController INSTANCE;

    static{
        INSTANCE = new SaveLoadController();
    }

    public static SaveLoadController getInstance() {
        return INSTANCE;
    }

    private File loadLocation = new File("./");
    public void load(PC pc, Scene scene) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(loadLocation);
        //Set extension filter for text files
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PF2 files", "*.pf2")
        );

        //Show save file dialog
        File file = fileChooser.showOpenDialog(scene.getWindow());
        if(file == null) return;
        saveLocation = file;
        loadLocation = saveLocation.getParentFile();
        SaveLoadManager.load(pc, saveLocation);

    }

    private File saveContainer = new File("./");
    public void saveAs(PC pc, Scene scene) {
        String characterName = pc.qualities().get("name");
        FileChooser fileChooser = new FileChooser();
        if(!Objects.equals(characterName, ""))
            fileChooser.setInitialFileName(characterName.replaceAll(" ","_"));
        fileChooser.setInitialDirectory(saveContainer);
        //Set extension filter for text files
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PF2 files", "*.pf2")
        );

        //Show save file dialog
        File file = fileChooser.showSaveDialog(scene.getWindow());
        if(file == null) return;
        saveLocation = file;
        saveContainer = saveLocation.getParentFile();
        SaveLoadManager.save(saveLocation);
    }

    public void save(PC pc, Scene scene) {
        if(saveLocation != null) {
            SaveLoadManager.save(saveLocation);
        }else{
            saveAs(pc, scene);
        }
    }

    public void export(String template, Scene scene) {
        FileChooser fileChooser = new FileChooser();
        if(exportLocation != null) {
            fileChooser.setInitialDirectory(exportLocation.getParentFile());
            fileChooser.setInitialFileName(exportLocation.getName());
        } else if(!Objects.equals(CharacterManager.getActive().qualities().get("name"), "")) {
            fileChooser.setInitialFileName(CharacterManager.getActive().qualities().get("name").replaceAll("[ ?!]",""));
            fileChooser.setInitialDirectory(new File("./"));
        }
        //Set extension filter for text files
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("HTML files", "*.html")
        );

        //Show save file dialog
        File file = fileChooser.showSaveDialog(scene.getWindow());

        if (file != null) {
            try {
                PrintWriter out = new PrintWriter(file);
                out.println(CharacterController.getFiller(CharacterManager.getActive()).getSheet(template));
                out.close();
                exportLocation = file;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
