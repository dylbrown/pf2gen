package ui.controllers;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.web.WebView;

import java.util.function.Consumer;
import java.util.function.Function;

public class SingleChoicePageController<T> {
    private final ReadOnlyObjectProperty<T> value;
    private final Consumer<T> adder;
    private final Function<T, String> htmlGenerator;
    @FXML
    private ListView<T> options, chosen;
    @FXML
    private WebView preview;
    private final ObservableList<T> optionsList;

    SingleChoicePageController(ObservableList<T> options, ReadOnlyObjectProperty<T> value,
                               Consumer<T> adder, Function<T, String> htmlGenerator) {
        optionsList = options;
        this.value = value;
        this.adder = adder;
        this.htmlGenerator = htmlGenerator;
    }

    @FXML
    private void initialize() {
        preview.getEngine().setUserStyleSheetLocation(getClass().getResource("/webview_style.css").toString());
        this.options.setItems(optionsList);
        value.addListener((o, oldVal, newVal) -> {
            if (newVal != null) {
                if(chosen.getItems().size() == 0)
                    chosen.getItems().add(newVal);
                else
                    chosen.getItems().set(0, newVal);
            } else {
                chosen.getItems().clear();
            }
        });
        options.setOnMouseClicked(e->{
            T selectedItem = options.getSelectionModel().getSelectedItem();
            if(selectedItem != null) {
                preview.getEngine().loadContent(htmlGenerator.apply(selectedItem));
                if(e.getClickCount() == 2){
                    adder.accept(null);
                    adder.accept(selectedItem);
                }
            }
        });
        chosen.setOnMouseClicked(e->{
            T selectedItem = chosen.getSelectionModel().getSelectedItem();
            if(selectedItem != null) {
                preview.getEngine().loadContent(htmlGenerator.apply(selectedItem));
                if(e.getClickCount() == 2)
                    adder.accept(null);
            }
        });
    }
}
