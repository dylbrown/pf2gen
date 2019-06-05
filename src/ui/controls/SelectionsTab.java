package ui.controls;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import model.abilities.abilitySlots.Choice;
import model.abilities.abilitySlots.ChoiceList;
import model.abilities.abilitySlots.ChoiceSlot;
import model.abilities.abilitySlots.FeatSlot;

import java.util.HashMap;
import java.util.Map;

import static ui.Main.character;

public class SelectionsTab extends AnchorPane {
    private final ComboBox<Choice> box = new ComboBox<>();
    private final AnchorPane container = new AnchorPane();
    private final Map<Choice, AnchorPane> panes = new HashMap<>();
    private ObservableList<Choice> decisions;
    public SelectionsTab(){
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
        container.getChildren().add(new Label());
        decisions = character.decisions().getDecisions();
        decisions.addListener((ListChangeListener<Choice>) (change)->{
            while(change.next()) {
                box.getItems().addAll(change.getAddedSubList());
                for (Choice choice : change.getRemoved()) {
                    box.getItems().remove(choice);
                    panes.remove(choice);
                }
            }
        });
        box.setOnAction((event)-> {
            if(box.getValue() != null)
                container.getChildren().set(0, panes.computeIfAbsent(box.getValue(), (choice)->{
                if(choice instanceof FeatSlot) {
                    return new FeatSelectionPane((FeatSlot)choice);
                }else if(choice instanceof ChoiceSlot){
                    return new FeatSelectionPane((ChoiceSlot)choice);
                }else if(choice instanceof ChoiceList){
                    return new SelectionPane<Object>((ChoiceList<Object>) choice);
                }else{
                    return new AnchorPane();
                }
                }));
            else {
                container.getChildren().clear();
                container.getChildren().add(new Label());
            }
        });
    }
}
