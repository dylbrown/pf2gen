package ui.controls.lists;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import model.CharacterManager;
import model.ability_slots.Choice;
import model.player.PC;
import ui.controls.SelectionPane;
import ui.controls.lists.entries.DecisionEntry;
import ui.controls.lists.factories.TreeCellFactory;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class DecisionsList extends SelectionPane<Choice<?>, DecisionEntry> {
    private final Consumer<Choice<?>> selectHandler;

    protected DecisionsList(Builder builder) {
        super(builder);
        selectHandler = builder.selectHandler;
        PC pc = CharacterManager.getActive();
        pc.abilities().addOnApplyListener(a->
                builder.filteredOptions.setPredicate(filterRemaining(pc, builder.filterByRemaining)));
        pc.abilities().addOnRemoveListener(a->
                builder.filteredOptions.setPredicate(filterRemaining(pc, builder.filterByRemaining)));
        builder.filterByRemaining.addListener((o, oldVal, newVal)->
                builder.filteredOptions.setPredicate(filterRemaining(pc, builder.filterByRemaining)));
        builder.filteredOptions.setPredicate(filterRemaining(pc, builder.filterByRemaining));
    }

    private Predicate<Choice<?>> filterRemaining(PC pc, ObservableValue<Boolean> filterByRemaining) {
       if(filterByRemaining.getValue()) {
           return c -> c.getMaxSelections() > c.numSelectionsProperty().getValue();
       }else return null;
    }

    @Override
    protected List<TreeTableColumn<DecisionEntry, ?>> makeColumns(ReadOnlyDoubleProperty width) {
        TreeTableColumn<DecisionEntry, String> name = new TreeTableColumn<>("Name");
        //TreeTableColumn<DecisionEntry, String> level = new TreeTableColumn<>("Level");
        TreeTableColumn<DecisionEntry, String> remaining = new TreeTableColumn<>("Remaining");
        name.setCellValueFactory(new TreeCellFactory<>("name"));
        // name.minWidthProperty().bind(this.widthProperty().multiply(.6));
        //level.setCellValueFactory(new TreeCellFactory<>("level"));
        //level.setStyle( "-fx-alignment: CENTER;");
        remaining.setCellValueFactory(new TreeCellFactory<>("remaining"));
        remaining.setStyle( "-fx-alignment: CENTER;");
        return Arrays.asList(name, remaining);
    }

    @Override
    protected ObservableCategoryEntryList<Choice<?>, DecisionEntry> makeList() {
        ObservableCategoryEntryList<Choice<?>, DecisionEntry> subList = new ObservableCategoryEntryList<>(
                this.list,
                (item, treeItem, clickCount) -> {
                    selectHandler.accept(item);
                    if(clickCount == 2) {
                        item.tryRemove(treeItem.getValue().getChosenValue());
                    }
                },
                categoryFunctionProperty.getValue().first,
                subCategoryFunctionProperty.getValue().first,
                makeEntry, makeLabelEntry,
                this::makeColumns);
        setupDecisionNode(subList.getRoot(), subList);
        subList.onInsert(node->setupDecisionNode(node, subList));
        return subList;
    }

    private static <T> ListChangeListener<T> getListener(Choice<T> choice, ObservableCategoryEntryList<Choice<?>, DecisionEntry> subList) {
        return c->{
            TreeItem<DecisionEntry> node = subList.findNode(choice);
            while(c.next()) {
                for (T t : c.getAddedSubList()) {
                    node.getChildren().add(new TreeItem<>(new DecisionEntry(choice, t)));
                }
                for (T t : c.getRemoved()) {
                    node.getChildren().removeIf(i->i.getValue().getChosenValue().equals(t));
                }

            }
        };
    }

    private void setupDecisionNode(TreeItem<DecisionEntry> node, ObservableCategoryEntryList<Choice<?>, DecisionEntry> subList) {
        if(node.getValue() == null || node.getValue().getChoice() == null) {
            for (TreeItem<DecisionEntry> child : node.getChildren()) {
                setupDecisionNode(child, subList);
            }
            return;
        }
        setupDecisionNode(node, node.getValue().getChoice(), subList);
    }

    private <T> void setupDecisionNode(TreeItem<DecisionEntry> node, Choice<T> choice, ObservableCategoryEntryList<Choice<?>, DecisionEntry> subList) {
        for (T selection : choice.getSelections()) {
            node.getChildren().add(new TreeItem<>(new DecisionEntry(choice, selection)));
        }
        choice.getSelections().addListener(getListener(choice, subList));
    }

    public static class Builder extends SelectionPane.Builder<Choice<?>, DecisionEntry> {
        private FilteredList<Choice<?>> filteredOptions;
        private Consumer<Choice<?>> selectHandler;
        private ObservableValue<Boolean> filterByRemaining;

        public Builder() {
            super(DecisionEntry::new, DecisionEntry::new);
        }

        public void setFilterByRemaining(ObservableValue<Boolean> filterByRemaining) {
            this.filterByRemaining = filterByRemaining;
        }

        public void setSelectHandler(Consumer<Choice<?>> selectHandler) {
            this.selectHandler = selectHandler;
        }

        @Override
        public void setOptions(ObservableList<Choice<?>> options) {
            FilteredList<Choice<?>> filteredOptions = new FilteredList<>(options, null);
            this.filteredOptions = filteredOptions;
            super.setOptions(filteredOptions);
        }

        @Override
        public DecisionsList build() {
            return new DecisionsList(this);
        }
    }
}
