package ui.controls.lists.entries;

import javafx.beans.property.IntegerProperty;
import javafx.scene.control.ListCell;
import model.creatures.CustomCreatureValue;
import model.creatures.scaling.ScaleMap;

public class CreatureChoiceCell<T> extends ListCell<CustomCreatureValue<T>> {
    private final CreatureChoiceCellController<T> controller;

    public CreatureChoiceCell(IntegerProperty level, ScaleMap scaleMap) {
        controller = new CreatureChoiceCellController<>(level, scaleMap);
    }

    @Override
    protected void updateItem(CustomCreatureValue<T> item, boolean empty) {
        super.updateItem(item, empty);
        if(empty) setGraphic(null);
        else {
            controller.setItem(item);
            setGraphic(controller.getCell());
        }
    }
}
