package ui.controls;

import javafx.collections.ListChangeListener;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import model.abilities.abilitySlots.AbilitySlot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ui.Main.character;

public class FeatsTab extends AnchorPane {
    private final ComboBox<AbilitySlot> box = new ComboBox<>();
    private final AnchorPane container = new AnchorPane();
    private final Map<AbilitySlot, FeatSelectionPane> panes = new HashMap<>();
    public FeatsTab(){
        GridPane grid = new GridPane();
        this.getChildren().add(grid);
        AnchorPane.setLeftAnchor(grid, 15.0);
        AnchorPane.setRightAnchor(grid, 15.0);
        AnchorPane.setTopAnchor(grid, 0.0);
        AnchorPane.setBottomAnchor(grid, 0.0);
        grid.setStyle("-fx-hgap: 10;");
        grid.addRow(0, box);
        grid.addRow(1, container);
        GridPane.setHgrow(container, Priority.ALWAYS);
        GridPane.setVgrow(container, Priority.ALWAYS);
        box.setItems(character.getDecisions());
        container.getChildren().add(new Label());
        character.getDecisions().addListener((ListChangeListener<? super AbilitySlot>) (event)-> updatePanes());
        updatePanes();
        box.setOnAction((event)-> container.getChildren().set(0, panes.computeIfAbsent(box.getValue(), FeatSelectionPane::new)));
    }

    private void updatePanes() {
        List<AbilitySlot> decisions = character.getDecisions();
        panes.entrySet().removeIf((entry)-> !decisions.contains(entry.getKey()));
    }
}
