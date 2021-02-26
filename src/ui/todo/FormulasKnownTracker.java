package ui.todo;

import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.collections.SetChangeListener;
import model.items.Item;
import model.player.PC;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class FormulasKnownTracker {
    private static final Map<PC, FormulasKnownTracker> trackers = new HashMap<>();

    public static ObservableMap<Integer, Integer> getFormulasRemainingTracker(PC character) {
        return trackers.computeIfAbsent(character, FormulasKnownTracker::new).unmodifiable;
    }
    private final PC character;
    ObservableMap<Integer, Integer> formulasRemaining = FXCollections.observableMap(new TreeMap<>());
    ObservableMap<Integer, Integer> unmodifiable = FXCollections.unmodifiableObservableMap(formulasRemaining);

    private FormulasKnownTracker(PC character) {
        this.character = character;
        character.inventory().getFormulasGranted().addListener(
                (SetChangeListener<Item>) change -> refresh());
        character.inventory().getGrantedFormulasCount().addListener(
                (MapChangeListener<Integer, Integer>) change -> refresh());
        refresh();
    }

    private void refresh() {
        TreeMap<Integer, Integer> tempMap = new TreeMap<>(character.inventory().getGrantedFormulasCount());
        for (Item item : character.inventory().getFormulasGranted()) {
            tempMap.merge(item.getLevel(), -1, Integer::sum);
        }
        for (Integer level : tempMap.keySet()) {
            Integer remaining = tempMap.get(level);
            if(remaining < 0) {
                for (Integer higherLevel : tempMap.tailMap(level, false).keySet()) {
                    Integer higherRemaining = tempMap.get(higherLevel);
                    if(higherRemaining > 0) {
                        remaining = tempMap.merge(level, higherRemaining, Integer::sum);
                        tempMap.merge(higherLevel, -higherRemaining, Integer::sum);
                        if(remaining >= 0)
                            break;
                    }
                }
            }
        }
        formulasRemaining.putAll(tempMap);
    }
}
