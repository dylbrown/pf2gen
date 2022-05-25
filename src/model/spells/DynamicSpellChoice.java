package model.spells;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.ability_slots.Choice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DynamicSpellChoice implements Choice<Spell> {
    private final String name;
    private final ObservableList<Spell> selections = FXCollections.observableArrayList();
    private final ReadOnlyIntegerWrapper numSelections = new ReadOnlyIntegerWrapper(0), maxSelections;
    private final List<Integer> levels;
    private final List<Tradition> traditions;

    private DynamicSpellChoice(Builder builder) {
        name = builder.name;
        maxSelections = new ReadOnlyIntegerWrapper(builder.maxSelections);
        levels = builder.levels;
        traditions = builder.traditions;
    }

    @Override
    public Class<Spell> getOptionsClass() {
        return Spell.class;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public void add(Spell choice) {
        if(selections.size() < getMaxSelections()) {
            selections.add(choice);
        }
    }

    @Override
    public void remove(Spell choice) {
        selections.remove(choice);
    }

    @Override
    public void empty() {
        selections.clear();
    }

    @Override
    public int getMaxSelections() {
        return maxSelections.get();
    }

    @Override
    public ReadOnlyIntegerProperty numSelectionsProperty() {
        return numSelections.getReadOnlyProperty();
    }

    @Override
    public ReadOnlyIntegerProperty maxSelectionsProperty() {
        return maxSelections.getReadOnlyProperty();
    }

    private final ObservableList<Spell> unmodifiableSelections = FXCollections.unmodifiableObservableList(selections);
    @Override
    public ObservableList<Spell> getSelections() {
        return unmodifiableSelections;
    }

    @Override
    public int getLevel() {
        return 0;
    }

    @Override
    public DynamicSpellChoice copy() {
        Builder builder = new Builder();
        builder.setName(name);
        builder.setMaxSelections(maxSelections.get());
        builder.levels = levels;
        builder.traditions = traditions;
        return builder.build();
    }

    public List<Integer> getLevels() {
        return Collections.unmodifiableList(levels);
    }

    public List<Tradition> getTraditions() {
        return Collections.unmodifiableList(traditions);
    }

    public static class Builder {
        private String name = "";
        private int maxSelections = 1;
        private List<Integer> levels = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        private List<Tradition> traditions = Arrays.asList(Tradition.values());

        public void setName(String name) {
            this.name = name;
        }

        public void setMaxSelections(int maxSelections) {
            this.maxSelections = maxSelections;
        }

        public void addTradition(Tradition tradition) {
            if(traditions.size() == Tradition.values().length)
                traditions = new ArrayList<>();
            traditions.add(tradition);
        }

        public void addLevel(int level) {
            if(levels.size() == 11)
                levels = new ArrayList<>();
            levels.add(level);
        }

        public DynamicSpellChoice build() {
            return new DynamicSpellChoice(this);
        }
    }
}
