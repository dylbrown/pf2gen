package ui.controls;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import model.abilities.abilitySlots.Choice;
import model.abilities.abilitySlots.ChoiceList;

import java.util.Comparator;

import static ui.Main.character;


public class SelectionPane<T> extends AnchorPane {
    ObservableList<T> items = FXCollections.observableArrayList();
    ListView<T> choices = new ListView<>();
    BorderPane side = new BorderPane();
    SelectionPane(ChoiceList<T> slot) {
        init(slot);
        items.addAll(slot.getOptions());
    }

    SelectionPane() {}

    void init(Choice<T> slot) {
        choices.setItems(new SortedList<>(items, Comparator.comparing(Object::toString)));
        Label selectedLabel = new Label("Selection: ");
        selectedLabel.setStyle("-fx-font-size: 20px");
        HBox selectRow = new HBox(selectedLabel);
        selectRow.setAlignment(Pos.CENTER);
        side.setBottom(selectRow);
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

        choices.setOnMouseClicked((event) -> {
            if(event.getClickCount() == 2) {
                T selectedItem = choices.getSelectionModel().getSelectedItem();
                if(selectedItem != null) {
                    character.choose(slot, selectedItem);
                }
            }
        });
        if(slot.getChoice() != null)
            selectedLabel.setText("Selection: " + slot.getChoice().toString());
        slot.getChoiceProperty().addListener((o, oldVal, newVal)->selectedLabel.setText("Selection: " + newVal.toString()));
    }
}
