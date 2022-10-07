package ui.controls;

import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import model.CharacterManager;
import model.abilities.Ability;
import model.ability_slots.Choice;
import model.ability_slots.FeatSlot;
import model.player.PC;
import ui.controls.lists.entries.AbilityEntry;

import java.util.function.Predicate;

public class FeatSelectionPane extends SelectionPane<Ability, AbilityEntry> {

    private FeatSelectionPane(Builder builder) {
        super(builder);
        PC pc = CharacterManager.getActive();
        pc.levelProperty().addListener((o, oldVal, newVal) -> refresh(builder));
        pc.abilities().addOnApplyListener(a -> refresh(builder));
        pc.abilities().addOnRemoveListener(a -> refresh(builder));
        pc.decisions().getDecisions().addListener((ListChangeListener<Choice<?>>) change -> refresh(builder));
        pc.attributes().addListener(evt -> refresh(builder));
        builder.filterByPrerequisites.addListener((o, oldVal, newVal)->refresh(builder));
        refresh(builder);
    }

    private void refresh(Builder builder) {
        builder.filteredOptions.setPredicate(
                filterPrerequisites(CharacterManager.getActive(), builder.filterByPrerequisites));
    }

    private Predicate<? super Ability> filterPrerequisites(PC pc, ObservableValue<Boolean> filterByPrerequisites) {
        if(filterByPrerequisites.getValue())
            return pc::meetsPrerequisites;
        else
            return null;
    }

    public static class Builder extends SelectionPane.Builder<Ability, AbilityEntry> {
        private FilteredList<Ability> filteredOptions;
        private ObservableValue<Boolean> filterByPrerequisites;

        public Builder() {
            super(AbilityEntry::new, AbilityEntry::new);
        }

        public void setFilterByPrerequisites(ObservableValue<Boolean> filterByPrerequisites) {
            this.filterByPrerequisites = filterByPrerequisites;
        }

        public void setChoice(FeatSlot slot) {
            super.setChoice(slot);
            setOptions(CharacterManager.getActive().abilities().getOptions(slot));
        }

        @Override
        public void setOptions(ObservableList<Ability> options) {
            FilteredList<Ability> filteredOptions = new FilteredList<>(options, null);
            this.filteredOptions = filteredOptions;
            super.setOptions(filteredOptions);
        }

        @Override
        public FeatSelectionPane build() {
            return new FeatSelectionPane(this);
        }
    }
}