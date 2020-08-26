package model;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.player.PC;

public abstract class CharacterManager {
    private static final ObservableList<PC> characters = FXCollections.observableArrayList(pc ->
            new Observable[]{pc.qualities().getProperty("name")});
    private static PC activeCharacter = null;

    public static void add(PC pc){
        characters.add(pc);
    }
    public static void remove(PC pc){
        characters.remove(pc);
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
