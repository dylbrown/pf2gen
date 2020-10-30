package model;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.player.PC;
import ui.ftl.wrap.ObjectWrapperCharacter;

import java.util.HashMap;
import java.util.Map;

import static ui.ftl.wrap.PF2GenObjectWrapper.CUSTOM_FORMATS;

public abstract class CharacterManager {
    private static final ObservableList<PC> characters = FXCollections.observableArrayList(pc ->
            new Observable[]{pc.qualities().getProperty("name")});
    private static Map<PC, Configuration> freemarkerConfigurations = new HashMap<>();
    private static PC activeCharacter = null;

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
        activeCharacter = pc;
    }
    public static PC getActive() {
        return activeCharacter;
    }

    public static ObservableList<PC> getCharacters() {
        return FXCollections.unmodifiableObservableList(characters);
    }
}
