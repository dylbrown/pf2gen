package ui.controls;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import model.abilities.abilitySlots.Choice;
import model.abilities.abilitySlots.ChoiceList;

import java.util.Comparator;

import static ui.Main.character;


class SelectionPane<T> extends AnchorPane {
    final ObservableList<T> items = FXCollections.observableArrayList();
    ObservableList<T> selections = FXCollections.observableArrayList();
    final ListView<T> choices = new ListView<>();
    final ListView<T> chosen = new ListView<>();
    final SplitPane side = new SplitPane();
    private Choice<T> slot;

    SelectionPane(ChoiceList<T> slot) {
        selections = slot.getSelections();
        init(slot);
        items.addAll(slot.getOptions());
        if(slot.getOptions() instanceof ObservableList)
            ((ObservableList<T>) slot.getOptions()).addListener((ListChangeListener<T>)change->{
                while(change.next()){
                    items.addAll(change.getAddedSubList());
                    items.removeAll(change.getRemoved());
                }
            });
    }

    SelectionPane() {
    }

    void init(Choice<T> slot) {
        this.slot = slot;
        choices.setItems(new SortedList<>(items, Comparator.comparing(Object::toString)));
        chosen.setItems(new SortedList<>(selections, Comparator.comparing(Object::toString)));
        BorderPane chosenPane = new BorderPane();
        chosenPane.setCenter(chosen);
        chosenPane.setTop(new Label("Selection(s)"));
        side.getItems().add(chosenPane);
        side.setOrientation(Orientation.VERTICAL);
        side.setDividerPositions(.25);
        SplitPane splitPane = new SplitPane(choices, side);
        getChildren().add(splitPane);
        AnchorPane.setLeftAnchor(splitPane, 0.0);
        AnchorPane.setRightAnchor(splitPane, 0.0);
        AnchorPane.setTopAnchor(splitPane, 0.0);
        AnchorPane.setBottomAnchor(splitPane, 0.0);
        AnchorPane.setLeftAnchor(this, 0.0);
        AnchorPane.setRightAnchor(this, 0.0);
        AnchorPane.setTopAnchor(this, 0.0);
        AnchorPane.setBottomAnchor(this, 0.0);

        setupChoicesListener();
        setupChosenListener();
    }

    void setupChoicesListener() {
        choices.setOnMouseClicked((event) -> {
            if(event.getClickCount() == 2) {
                T selectedItem = choices.getSelectionModel().getSelectedItem();
                if(selectedItem != null && slot.getNumSelections() > slot.getSelections().size()) {
                    character.addSelection(slot, selectedItem);
                }
            }
        });
    }
    void setupChosenListener() {
        chosen.setOnMouseClicked((event) -> {
            if(event.getClickCount() == 2) {
                T selectedItem = chosen.getSelectionModel().getSelectedItem();
                character.removeSelection(slot, selectedItem);
            }
        });
    }
}
