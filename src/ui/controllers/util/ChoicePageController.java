package ui.controllers.util;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.web.WebView;
import model.ability_slots.Choice;
import model.ability_slots.ChoiceList;
import model.util.ObservableUtils;

import java.util.Objects;
import java.util.function.Function;

public class ChoicePageController<T> {
    private final Function<T, String> htmlGenerator;
    private final Choice<T> choice;
    @FXML
    private ListView<T> options, chosen;
    @FXML
    private WebView preview;
    private final ObservableList<T> optionsList;

    public ChoicePageController(ObservableList<T> options, Choice<T> choice, Function<T, String> htmlGenerator) {
        optionsList = options;
        this.choice = choice;
        this.htmlGenerator = htmlGenerator;
    }

    ChoicePageController(ChoiceList<T> choice, Function<T, String> htmlGenerator) {
        this(ObservableUtils.makeList(choice.getOptions()), choice, htmlGenerator);
    }

    @FXML
    private void initialize() {
        preview.getEngine().setUserStyleSheetLocation(
                Objects.requireNonNull(getClass().getResource("/webview_style.css")).toString());
        this.options.setItems(optionsList);
        this.chosen.setItems(choice.getSelections());
        options.setOnMouseClicked(e->{
            T selectedItem = options.getSelectionModel().getSelectedItem();
            if(selectedItem != null) {
                if(e.getClickCount() == 2){
                    choice.add(selectedItem);
                }
            }
        });
        options.getSelectionModel().selectedItemProperty().addListener(c->{
            T selectedItem = options.getSelectionModel().getSelectedItem();
            if(selectedItem != null)
                preview.getEngine().loadContent(htmlGenerator.apply(selectedItem));
        });
        chosen.setOnMouseClicked(e->{
            T selectedItem = chosen.getSelectionModel().getSelectedItem();
            if(selectedItem != null) {
                preview.getEngine().loadContent(htmlGenerator.apply(selectedItem));
                if(e.getClickCount() == 2)
                    choice.remove(selectedItem);
            }
        });
    }
}
