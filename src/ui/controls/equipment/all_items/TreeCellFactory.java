package ui.controls.equipment.all_items;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;

public class TreeCellFactory implements Callback<TreeTableColumn.CellDataFeatures<ItemEntry, String>, ObservableValue<String>> {
    private final String propertyName;

    TreeCellFactory(String property) {
        this.propertyName = property;
    }

    @Override
    public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<ItemEntry, String> f) {
        return f.getValue().getValue().get(propertyName);
    }
}
