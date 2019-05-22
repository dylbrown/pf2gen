package ui.controls;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import model.abilities.Ability;
import model.abilities.abilitySlots.AbilitySlot;
import model.abilities.abilitySlots.FeatSlot;
import model.abilities.abilitySlots.Pickable;
import model.enums.Type;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static ui.Main.character;


public class FeatSelectionPane extends AnchorPane {
    private AbilitySlot slot;
    private List<Ability> unmetAndTaken = new ArrayList<>();
    ObservableList<Ability> items = FXCollections.observableArrayList();

    public FeatSelectionPane(AbilitySlot slot) {

        if(!(slot instanceof Pickable)) return;
        this.slot = slot;
        BorderPane side = new BorderPane();
        Label desc = new Label();
        desc.setWrapText(true);
        ListView<Ability> choices = new ListView<>();
        choices.setItems(new SortedList<>(items, Comparator.comparing(Ability::toString)));
        items.addAll(((Pickable)slot).getAbilities(slot.getLevel()));
        items.removeIf((item)->{
            if(!character.meetsPrerequisites(item) || character.getAbilities().contains(item)){
                unmetAndTaken.add(item);
                return true;
            }
            return false;
        });
        character.getAbilities().addListener((ListChangeListener<Ability>) (event)->{
            while(event.next()) {
                if (event.wasAdded()) {
                    items.removeAll(event.getAddedSubList());
                    unmetAndTaken.removeIf((item) -> {
                        if (character.meetsPrerequisites(item) && !character.getAbilities().contains(item)) {
                            items.add(item);
                            return true;
                        }
                        return false;
                    });
                    unmetAndTaken.addAll(event.getAddedSubList());
                }
                if (event.wasRemoved()) {
                    unmetAndTaken.removeAll(event.getRemoved());
                    items.addAll(event.getRemoved());
                    items.removeIf((item) -> {
                        if (!character.meetsPrerequisites(item) || character.getAbilities().contains(item)) {
                            unmetAndTaken.add(item);
                            return true;
                        }
                        return false;
                    });
                }
            }
        });
        Label selectedLabel = new Label("Selection: ");
        selectedLabel.setStyle("-fx-font-size: 20px");
        HBox selectRow = new HBox(selectedLabel);
        selectRow.setAlignment(Pos.CENTER);
        side.setTop(desc);
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
        choices.getSelectionModel().selectedItemProperty().addListener((event)->{
            Ability selectedItem = choices.getSelectionModel().getSelectedItem();
            if(selectedItem != null)
                desc.setText(selectedItem.getDesc());
        });
        choices.setOnMouseClicked((event) -> {
            if(event.getClickCount() == 2) {
                Ability selectedItem = choices.getSelectionModel().getSelectedItem();
                if(selectedItem != null) {
                    character.choose(slot, selectedItem);
                    selectedLabel.setText("Selection: " + selectedItem.toString());
                }
            }
        });
        if(slot instanceof FeatSlot && ((FeatSlot) slot).getAllowedTypes().contains(Type.Ancestry)) {
            character.addAncestryObserver((observable, arg)->{
                items.setAll(((Pickable)slot).getAbilities(slot.getLevel()));
                FXCollections.sort(items, Comparator.comparing(Ability::toString));
            });
        }
    }
}
