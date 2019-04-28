package ui.customControls;

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
        choices.getItems().addAll(((Pickable)slot).getAbilities());
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
            double[] dividerPositions = splitPane.getDividerPositions();
            desc.setText(choices.getSelectionModel().getSelectedItem().getDesc());
            splitPane.setDividerPositions(dividerPositions);
        });
        select.setOnAction((event -> {
            character.choose(slot, choices.getSelectionModel().getSelectedItem());
            selectedLabel.setText("Selection: "+choices.getSelectionModel().getSelectedItem().toString());
        }));
        if(slot instanceof FeatSlot && ((FeatSlot) slot).getAllowedTypes().contains(Type.Ancestry)) {
            character.addAncestryObserver((observable, arg)->choices.getItems().setAll(((Pickable)slot).getAbilities()));
        }
    }
}
