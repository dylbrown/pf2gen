package ui.controls;

import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
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

import java.util.Comparator;

import static ui.Main.character;


public class FeatSelectionPane extends AnchorPane {
    private AbilitySlot slot;

    public FeatSelectionPane(AbilitySlot slot) {

        if(!(slot instanceof Pickable)) return;
        this.slot = slot;
        BorderPane side = new BorderPane();
        Label desc = new Label();
        desc.setWrapText(true);
        ListView<Ability> choices = new ListView<>();
        choices.getItems().addAll(((Pickable)slot).getAbilities(slot.getLevel()));
        FXCollections.sort(choices.getItems(), Comparator.comparing(Ability::toString));
        Button select = new Button("Select");
        select.setStyle("size:100px");
        Label selectedLabel = new Label("Selection: ");
        selectedLabel.setStyle("size:100px");
        HBox selectRow = new HBox(select, selectedLabel);
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
        select.setOnAction((event -> {
            Ability selectedItem = choices.getSelectionModel().getSelectedItem();
            if(selectedItem != null) {
                character.choose(slot, selectedItem);
                selectedLabel.setText("Selection: " + selectedItem.toString());
            }
        }));
        if(slot instanceof FeatSlot && ((FeatSlot) slot).getAllowedTypes().contains(Type.Ancestry)) {
            character.addAncestryObserver((observable, arg)->{
                choices.getItems().setAll(((Pickable)slot).getAbilities(slot.getLevel()));
                FXCollections.sort(choices.getItems(), Comparator.comparing(Ability::toString));
            });
        }
    }
}
