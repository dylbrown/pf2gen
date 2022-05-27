package model;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.data_managers.SaveLoadManager;
import model.player.PC;
import org.apache.commons.io.output.StringBuilderWriter;
import ui.ftl.wrap.ObjectWrapperCharacter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ui.ftl.wrap.PF2GenObjectWrapper.CUSTOM_FORMATS;

public abstract class CharacterManager {
    private static final ObservableList<PC> characters = FXCollections.observableArrayList(pc ->
            new Observable[]{pc.qualities().getProperty("name")});
    private static final Map<PC, Configuration> freemarkerConfigurations = new HashMap<>();
    private static final ReadOnlyObjectWrapper<PC> activeCharacter = new ReadOnlyObjectWrapper<>();

    public static void add(PC pc){
        characters.add(pc);
    }
    public static void remove(PC pc){
        characters.remove(pc);
        freemarkerConfigurations.remove(pc);
    }
    public static Configuration getFreemarkerConfiguration(PC pc) {
        return freemarkerConfigurations.computeIfAbsent(pc, p->{
            Configuration cfg = new Configuration(Configuration.VERSION_2_3_29);
            cfg.setDefaultEncoding("UTF-8");
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            cfg.setLogTemplateExceptions(false);
            cfg.setWrapUncheckedExceptions(true);
            cfg.setObjectWrapper(new ObjectWrapperCharacter(Configuration.VERSION_2_3_29, p));
            cfg.setCustomNumberFormats(CUSTOM_FORMATS);
            return cfg;
        });
    }
    public static void setActive(PC pc) {
        activeCharacter.set(pc);
    }
    public static PC getActive() {
        return activeCharacter.get();
    }
    public static ReadOnlyObjectProperty<PC> activeProperty() {
        return activeCharacter.getReadOnlyProperty();
    }

    public static ObservableList<PC> getCharacters() {
        return FXCollections.unmodifiableObservableList(characters);
    }

    public static void reload(PC pc) {
        boolean active = getActive() == pc;
        StringBuilderWriter writer = new StringBuilderWriter();
        try {
            SaveLoadManager.save(pc, writer);
            List<String> lineList = writer.toString().lines().collect(Collectors.toList());
            PC newPC = new PC(SaveLoadManager.loadSources(lineList));
            SaveLoadManager.load(newPC, lineList);
            CharacterManager.add(newPC);
            if(active)
                setActive(newPC);
            remove(pc);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
