package ui.controls;

import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import model.CharacterManager;
import model.abilities.Ability;
import model.ability_slots.FeatSlot;
import model.player.PC;
import ui.controls.lists.entries.AbilityEntry;

import java.util.function.Predicate;

public class FeatSelectionPane extends SelectionPane<Ability, AbilityEntry> {

    private FeatSelectionPane(Builder builder) {
        super(builder);
        PC pc = CharacterManager.getActive();
        pc.abilities().addOnApplyListener(a->
                builder.filteredOptions.setPredicate(filterPrerequisites(pc, builder.filterByPrerequisites)));
        pc.abilities().addOnRemoveListener(a->
                builder.filteredOptions.setPredicate(filterPrerequisites(pc, builder.filterByPrerequisites)));
        builder.filterByPrerequisites.addListener((o, oldVal, newVal)->
                builder.filteredOptions.setPredicate(filterPrerequisites(pc, builder.filterByPrerequisites)));
        builder.filteredOptions.setPredicate(filterPrerequisites(pc, builder.filterByPrerequisites));
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