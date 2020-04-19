package ui.controls;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.web.WebView;
import model.abilities.abilitySlots.Choice;
import model.abilities.abilitySlots.ChoiceList;

import java.util.Comparator;

import static ui.Main.character;


public class SelectionPane<T> extends ListView<T> {
    final ObservableList<T> items = FXCollections.observableArrayList();
    final ObservableList<T> sortedItems = new SortedList<>(items, Comparator.comparing(Object::toString));
    WebView display;
    ObservableList<T> selections = FXCollections.observableArrayList();
    final ListView<T> chosen = new ListView<>();
    final SplitPane side = new SplitPane();
    private Choice<T> slot;

    public SelectionPane(ChoiceList<T> slot, WebView display) {
        this.display = display;
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

    SelectionPane() {}

    void init(Choice<T> slot) {
        this.slot = slot;
        setItems(sortedItems);
        chosen.setItems(new SortedList<>(selections, Comparator.comparing(Object::toString)));

        setupChoicesListener();
        setupChosenListener();
    }

    void setupChoicesListener() {
        setOnMouseClicked((event) -> {
            if(event.getClickCount() == 2) {
                T selectedItem = getSelectionModel().getSelectedItem();
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
