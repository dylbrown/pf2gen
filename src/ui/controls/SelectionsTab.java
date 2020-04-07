package ui.controls;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import model.abilities.abilitySlots.*;
import model.player.ArbitraryChoice;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.ToIntFunction;

import static ui.Main.character;

public class SelectionsTab extends AnchorPane {
    private final ComboBox<Choice> box = new ComboBox<>();
    private final ObservableList<Choice> boxItems = FXCollections.observableArrayList();
    private final AnchorPane container = new AnchorPane();
    private final Map<Choice, AnchorPane> panes = new HashMap<>();

    public SelectionsTab(){
        box.setVisible(false);
        GridPane grid = new GridPane();
        this.getChildren().add(grid);
        box.setItems(new SortedList<>(boxItems, (Comparator.comparingInt((ToIntFunction<Choice>) Choice::getLevel).thenComparing(Object::toString))));
        box.getItems().addListener((ListChangeListener<Choice>) (change)->{
            if(box.getItems().size() > 0)
                box.setVisible(true);
            else
                box.setVisible(false);
        });
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
        ObservableList<Choice> decisions = character.decisions().getDecisions();
        decisions.addListener((ListChangeListener<Choice>) (change)->{
            while(change.next()) {
                boxItems.addAll(change.getAddedSubList());
                for (Choice choice : change.getRemoved()) {
                    boxItems.remove(choice);
                    panes.remove(choice);
                }
            }
        });
        box.setOnAction((event)-> {
            if(box.getValue() != null)
                container.getChildren().set(0, panes.computeIfAbsent(box.getValue(), (choice)->{
                if(choice instanceof FeatSlot) {
                    return new FeatSelectionPane((FeatSlot)choice);
                }else if(choice instanceof SingleChoiceSlot){
                    return new FeatSelectionPane((SingleChoiceSlot)choice);
                }else if(choice instanceof SingleChoiceList){
                    return new SingleSelectionPane<>((SingleChoiceList<?>) choice);
                }else if (choice instanceof ArbitraryChoice) {
                    return new SelectionPane<>((ArbitraryChoice) choice);
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
