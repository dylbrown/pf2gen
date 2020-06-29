package ui.controls;

import javafx.scene.Scene;
import javafx.stage.FileChooser;
import model.data_managers.SaveLoadManager;
import ui.ftl.TemplateFiller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Objects;

import static ui.Main.character;

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
    public void load(Scene scene) {
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
        SaveLoadManager.load(saveLocation);

    }

    private File saveContainer = new File("./");
    public void saveAs(String characterName, Scene scene) {
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

    public void save(String characterName, Scene scene) {
        if(saveLocation != null) {
            SaveLoadManager.save(saveLocation);
        }else{
            saveAs(characterName, scene);
        }
    }

    public void export(String template, Scene scene) {
        FileChooser fileChooser = new FileChooser();
        if(exportLocation != null) {
            fileChooser.setInitialDirectory(exportLocation.getParentFile());
            fileChooser.setInitialFileName(exportLocation.getName());
        } else if(!Objects.equals(character.qualities().get("name"), "")) {
            fileChooser.setInitialFileName(character.qualities().get("name").replaceAll("[ ?!]",""));
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
                out.println(TemplateFiller.getInstance().getSheet(template));
                out.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void reset() {
        SaveLoadManager.reset();
    }
}
