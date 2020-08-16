package ui.controls;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import model.ability_slots.Choice;
import model.ability_slots.ChoiceList;
import ui.html.HTMLGenerator;

import java.util.Comparator;
import java.util.function.Function;


public class SelectionPane<T> extends BorderPane {
    final ObservableList<T> items = FXCollections.observableArrayList();
    final ObservableList<T> sortedItems = new SortedList<>(items, Comparator.comparing(Object::toString));
    private final Function<T, String> htmlGenerator;
    WebView display;
    ObservableList<T> selections = FXCollections.observableArrayList();
    final ListView<T> chosen = new ListView<>();
    final SplitPane side = new SplitPane();
    Choice<T> slot;
    private MultipleSelectionModel<T> selectionModel;
    private ListView<T> list;

    public SelectionPane(ChoiceList<T> slot, WebView display) {
        list = new ListView<>(sortedItems);
        this.setCenter(list);
        selectionModel = list.getSelectionModel();
        init(slot);
        htmlGenerator = HTMLGenerator.getGenerator(slot.getOptionsClass());
        this.display = display;
        selections = slot.getSelections();
        items.addAll(slot.getOptions());
        items.removeAll(slot.getSelections());
        if(slot.getOptions() instanceof ObservableList)
            ((ObservableList<T>) slot.getOptions()).addListener((ListChangeListener<T>)change->{
                while(change.next()){
                    items.addAll(change.getAddedSubList());
                    items.removeAll(change.getRemoved());
                }
            });
        slot.getSelections().addListener((ListChangeListener<T>) c->{
            while(c.next()) {
                items.removeAll(c.getAddedSubList());
                items.addAll(c.getRemoved());
            }
        });
    }

    protected MultipleSelectionModel<T> getSelectionModel() {
        return selectionModel;
    }

    SelectionPane() {
        htmlGenerator = (o) -> "";
    }

    void init(Choice<T> slot) {
        this.slot = slot;
        chosen.setItems(new SortedList<>(selections, Comparator.comparing(Object::toString)));

        setupChoicesListener();
        setupChosenListener();
    }

    void setupChoicesListener() {
        selectionModel.selectedItemProperty().addListener((o, oldVal, newVal)->{
            if(newVal != null) {
                display.getEngine().loadContent(htmlGenerator.apply(newVal));
            }
        });
        list.setOnMouseClicked((event) -> {
            if(event.getClickCount() % 2 == 0) {
                T selectedItem = selectionModel.getSelectedItem();
                if(selectedItem != null && slot.getMaxSelections() > slot.getSelections().size()) {
                    slot.add(selectedItem);
                }
            }
        });
    }
    void setupChosenListener() {
        chosen.setOnMouseClicked((event) -> {
            if(event.getClickCount() == 2) {
                T selectedItem = chosen.getSelectionModel().getSelectedItem();
                slot.remove(selectedItem);
            }
        });
    }
}
