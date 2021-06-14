package ui.controls.lists.entries;

import javafx.beans.property.IntegerProperty;
import javafx.scene.control.ListCell;
import model.creatures.CustomStrike;

import java.util.function.Consumer;

public class CustomStrikeCell extends ListCell<CustomStrike> {
    private final CustomStrikeCellController controller;

    public CustomStrikeCell(IntegerProperty level, Consumer<CustomStrike> delete) {
        controller = new CustomStrikeCellController(level, delete);
    }

    @Override
    protected void updateItem(CustomStrike item, boolean empty) {
        super.updateItem(item, empty);
        if(empty) {
            controller.setItem(null);
            setGraphic(null);
        }else {
            controller.setItem(item);
            setGraphic(controller.getCell());
        }
    }
}
