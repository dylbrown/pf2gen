package model.player;

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import model.abilities.SpellExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class SpellListManager implements PlayerState {
    private final ObservableMap<String, SpellList> spellLists = FXCollections.observableMap(new TreeMap<>());
    private final Map<String, Integer> abilityCountTracker = new HashMap<>();

    SpellListManager(Applier applier) {
        applier.onApply(ability -> {
            SpellExtension spellExtension = ability.getExtension(SpellExtension.class);
            if(spellExtension != null) {
                String index = spellExtension.getSpellListName();
                spellLists.computeIfAbsent(index, SpellList::new).apply(ability);
                abilityCountTracker.merge(index, 1, Integer::sum);
            }
        });
        applier.onRemove(ability -> {
            SpellExtension spellExtension = ability.getExtension(SpellExtension.class);
            if(spellExtension != null) {
                String index = spellExtension.getSpellListName();
                abilityCountTracker.merge(index, -1, Integer::sum);
                spellLists.get(index).remove(ability);
                if(abilityCountTracker.get(index) == 0) {
                    abilityCountTracker.remove(index);
                    spellLists.remove(index);
                }
            }
        });
    }

    private final ObservableMap<String, SpellList> unmodSpellLists =
            FXCollections.unmodifiableObservableMap(spellLists);
    public ObservableMap<String, SpellList> getSpellLists() {
        return unmodSpellLists;
    }

    public SpellList getSpellList(String spellListName) {
        if(spellListName == null || spellListName.equals(""))
            System.out.println("Warning: Bad SpellList Name");
        return spellLists.get(spellListName);
    }

    public int getFocusPointCount() {
        int focusPoints = 0;
        for (SpellList value : spellLists.values()) {
            focusPoints += value.getFocusPointCount();
            if(focusPoints >= 3) break;
        }
        return Math.min(3, focusPoints);
    }

    @Override
    public void reset(PC.ResetEvent resetEvent) {
        spellLists.clear();
    }
}
