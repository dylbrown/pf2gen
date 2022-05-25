package ui.controls;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.CharacterManager;
import model.data_managers.sources.SpellsMultiSourceLoader;
import model.spells.DynamicSpellChoice;
import model.spells.Spell;
import model.spells.Tradition;
import model.util.Pair;
import ui.controls.lists.entries.SpellEntry;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class SpellSelectionPane extends SelectionPane<Spell, SpellEntry> {

    private SpellSelectionPane(Builder builder) {
        super(builder);
    }

    public static class Builder extends SelectionPane.Builder<Spell, SpellEntry> {
        public Builder() {
            super(SpellEntry::new, SpellEntry::new);
        }

        public void setChoice(DynamicSpellChoice choice) {
            super.setChoice(choice);
            Set<Spell> options = new TreeSet<>(Comparator.comparing(Spell::getLevel).thenComparing(Spell::getName));
            SpellsMultiSourceLoader spells = CharacterManager.getActive().sources().spells();
            for (Integer level : choice.getLevels()) {
                for(Tradition tradition : choice.getTraditions()) {
                    options.addAll(spells.getSpells(tradition, level));
                    options.addAll(spells.getHeightenedSpells(tradition, level));
                }
            }
            super.setOptions(FXCollections.observableArrayList(options));
        }

        @Override
        public void setOptions(List<Spell> options) {
            throw new UnsupportedOperationException("Spells are determined by the choice");
        }

        @Override
        public void setOptions(ObservableList<Spell> options) {
            throw new UnsupportedOperationException("Spells are determined by the choice");
        }

        @Override
        public SpellSelectionPane build() {
            if(categoryFunctionProperty == null)
                categoryFunctionProperty = new SimpleObjectProperty<>(new Pair<>((t)->String.valueOf(t.getLevel()), "Level"));
            if(subCategoryFunctionProperty == null)
                subCategoryFunctionProperty = new SimpleObjectProperty<>(new Pair<>((t)->"", ""));
            return new SpellSelectionPane(this);
        }
    }
}