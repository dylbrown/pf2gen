package model.player;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import model.attributes.Attribute;
import model.attributes.AttributeMod;
import model.enums.Proficiency;

import java.util.*;

import static model.player.AttributeManager.updateProficiencyToMatchTracker;

public class SingleAttributeManager {
    private final Attribute attribute;
    private final Map<String, ReadOnlyObjectWrapper<Proficiency>> proficiencies = new HashMap<>();
    private final Map<String, Map<Proficiency, List<AttributeMod>>> trackers = new HashMap<>();

    SingleAttributeManager(Attribute attribute) {
        this.attribute = attribute;
    }


    /**
     * Get Proficiency Wrapper for attribute
     * Note: Internal function to reduce repeated computeIfAbsent
     * @param data The data string to get the wrapper for
     * @return The Attribute's proficiency wrapper
     */
    private ReadOnlyObjectWrapper<Proficiency> get(String data) {
        return proficiencies.computeIfAbsent(data, t->new ReadOnlyObjectWrapper<>(Proficiency.Untrained));
    }

    /**
     * @param mod the attributeMod to apply
     * @return returns true if the mod has changed the skill's proficiency
     */
    boolean apply(AttributeMod mod) {
        if(mod.getAttr() != attribute) return false;
        trackers
                .computeIfAbsent(mod.getData(), (data)->new HashMap<>())
                .computeIfAbsent(mod.getMod(), (key) -> new ArrayList<>())
                .add(mod);
        ReadOnlyObjectWrapper<Proficiency> proficiency = get(mod.getData());
        if(proficiency.getValue().getMod() < mod.getMod().getMod()) {
            proficiency.set(mod.getMod());
            return true;
        }
        return false;
    }

    /**
     * @param mod the attributeMod to remove
     * @return true if removing the mod has reduced the proficiency
     */
    boolean remove(AttributeMod mod) {
        if(mod.getAttr() != attribute) return false;
        Map<Proficiency, List<AttributeMod>> tracker = trackers
                .computeIfAbsent(mod.getData(), (key) -> new HashMap<>());
        List<AttributeMod> attributeMods = tracker
                .computeIfAbsent(mod.getMod(), (key) -> new ArrayList<>());
        if(!attributeMods.contains(mod)) return false;
        attributeMods.remove(mod);
        return updateProficiencyToMatchTracker(tracker, get(mod.getData()), mod);
    }

    /**
     * Check if we have a mod of the particular proficiency for data
     * @param data Data to check mods of
     * @param proficiency Level at which to check
     * @return returns true if we have a mod at the level of proficiency for data
     */
    boolean hasMod(String data, Proficiency proficiency) {
        Map<Proficiency, List<AttributeMod>> map = trackers.get(data);
        if(map == null) return false;
        List<AttributeMod> list = map.get(proficiency);
        if(list == null) return false;
        return list.size() > 0;
    }

    public ObservableValue<Proficiency> getProficiency(String data) {
        return get(data).getReadOnlyProperty();
    }

    Set<String> getDataStrings() {
        TreeSet<String> strings = new TreeSet<>(proficiencies.keySet());
        strings.removeIf(s->getProficiency(s).getValue().equals(Proficiency.Untrained));
        return strings;
    }
}
