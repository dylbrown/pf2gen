package ui.controls.lists.entries;

import javafx.beans.property.IntegerProperty;
import javafx.scene.control.ListCell;
import model.creatures.CustomCreatureValue;

public class CreatureChoiceCell<T> extends ListCell<CustomCreatureValue<T>> {
    private final CreatureChoiceCellController<T> controller;

    public CreatureChoiceCell(IntegerProperty level) {
        controller = new CreatureChoiceCellController<>(level);
    }

    @Override
    protected void updateItem(CustomCreatureValue<T> item, boolean empty) {
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
